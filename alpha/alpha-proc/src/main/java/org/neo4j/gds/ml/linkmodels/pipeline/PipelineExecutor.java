/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.gds.ml.linkmodels.pipeline;

import org.neo4j.gds.BaseProc;
import org.neo4j.gds.NodeLabel;
import org.neo4j.gds.RelationshipType;
import org.neo4j.gds.api.Graph;
import org.neo4j.gds.core.loading.GraphStoreCatalog;
import org.neo4j.gds.core.utils.paged.HugeObjectArray;
import org.neo4j.gds.ml.linkmodels.pipeline.linkFeatures.LinkFeatureExtractor;
import org.neo4j.kernel.database.NamedDatabaseId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.neo4j.gds.ml.linkmodels.pipeline.linkFeatures.LinkFeatureExtractor.extractFeatures;

public class PipelineExecutor {
    private final PipelineModelInfo pipeline;
    private final String userName;
    private final NamedDatabaseId databaseId;
    private final BaseProc caller;

    public PipelineExecutor(
        PipelineModelInfo pipeline,
        BaseProc caller,
        NamedDatabaseId databaseId,
        String userName
    ) {
        this.pipeline = pipeline;
        this.caller = caller;
        this.userName = userName;
        this.databaseId = databaseId;
    }

    public HugeObjectArray<double[]> computeFeatures(
        String graphName,
        Collection<NodeLabel> nodeLabels,
        RelationshipType relationshipType,
        int concurrency
    ) {
        var graph = GraphStoreCatalog.get(userName, databaseId, graphName)
            .graphStore()
            .getGraph(nodeLabels, List.of(relationshipType), Optional.empty());

        pipeline.validate(graph);

        return extractFeatures(graph, pipeline.featureSteps(), concurrency);
    }

    public LinkFeatureExtractor linkFeatureExtractor(Graph graph) {
        return LinkFeatureExtractor.of(graph, pipeline.featureSteps());
    }

    public void executeNodePropertySteps(
        String graphName,
        Collection<NodeLabel> nodeLabels,
        RelationshipType relationshipType
    ) {
            executeNodePropertySteps(graphName, nodeLabels, List.of(relationshipType));
    }

    public void executeNodePropertySteps(
        String graphName,
        Collection<NodeLabel> nodeLabels,
        Collection<RelationshipType> relationshipTypes
    ) {
        for (NodePropertyStep step : pipeline.nodePropertySteps()) {
            step.execute(caller, graphName, nodeLabels, relationshipTypes);
        }
    }
}