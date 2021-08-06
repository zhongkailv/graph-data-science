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
package org.neo4j.gds.ml.linkmodels.pipeline.predict;

import org.neo4j.gds.AbstractAlgorithmFactory;
import org.neo4j.gds.BaseProc;
import org.neo4j.gds.api.Graph;
import org.neo4j.gds.core.loading.GraphStoreCatalog;
import org.neo4j.gds.core.utils.mem.AllocationTracker;
import org.neo4j.gds.core.utils.mem.MemoryEstimation;
import org.neo4j.gds.core.utils.progress.v2.tasks.ProgressTracker;
import org.neo4j.gds.exceptions.MemoryEstimationNotImplementedException;
import org.neo4j.gds.ml.linkmodels.pipeline.LinkPredictionModelInfo;
import org.neo4j.gds.ml.linkmodels.pipeline.PipelineExecutor;
import org.neo4j.kernel.database.NamedDatabaseId;

import static org.neo4j.gds.ml.linkmodels.pipeline.PipelineUtils.getLinkPredictionPipeline;

public class LinkPredictionPipelineAlgorithmFactory<CONFIG extends LinkPredictionPipelineBaseConfig> extends AbstractAlgorithmFactory<LinkPrediction, CONFIG> {
    private final BaseProc caller;
    private final NamedDatabaseId databaseId;

    LinkPredictionPipelineAlgorithmFactory(
        BaseProc caller,
        NamedDatabaseId databaseId
    ) {
        super();
        this.caller = caller;
        this.databaseId = databaseId;
    }

    @Override
    protected String taskName() {
        return "Link Prediction Pipeline";
    }

    @Override
    protected LinkPrediction build(
        Graph graph, CONFIG configuration, AllocationTracker tracker, ProgressTracker progressTracker
    ) {
        String graphName = configuration
            .graphName()
            .orElseThrow(() -> new UnsupportedOperationException(
                "Link Prediction Pipeline cannot be used with anonymous graphs. Please load the graph before"));

        var model = getLinkPredictionPipeline(
            configuration.modelName(),
            configuration.username()
        );
        var graphStore = GraphStoreCatalog.get(configuration.username(), databaseId, graphName).graphStore();
        var customInfo = (LinkPredictionModelInfo) model.customInfo();
        var pipelineExecutor = new PipelineExecutor(customInfo.trainingPipeline(), caller, databaseId, configuration.username(), graphName);
        var nodeLabels = configuration.nodeLabelIdentifiers(graphStore);
        var relationshipTypes = configuration.internalRelationshipTypes(graphStore);
        return new LinkPrediction(
            model.data(),
            pipelineExecutor,
            nodeLabels,
            relationshipTypes,
            graph,
            configuration.concurrency(),
            configuration.topN(),
            configuration.threshold(),
            progressTracker
        );
    }

    @Override
    public MemoryEstimation memoryEstimation(CONFIG configuration) {
        throw new MemoryEstimationNotImplementedException();
    }
}