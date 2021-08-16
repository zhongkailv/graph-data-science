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
package org.neo4j.gds.doc;

import org.neo4j.gds.catalog.GraphCreateProc;
import org.neo4j.gds.ml.linkmodels.pipeline.LinkPredictionPipelineAddStepProcs;
import org.neo4j.gds.ml.linkmodels.pipeline.LinkPredictionPipelineConfigureParamsProc;
import org.neo4j.gds.ml.linkmodels.pipeline.LinkPredictionPipelineConfigureSplitProc;
import org.neo4j.gds.ml.linkmodels.pipeline.LinkPredictionPipelineCreateProc;
import org.neo4j.gds.ml.linkmodels.pipeline.LinkPredictionPipelineTrainProc;
import org.neo4j.gds.ml.linkmodels.pipeline.predict.LinkPredictionPipelineMutateProc;

import java.util.Arrays;
import java.util.List;

class LinkPredictionPipelineDocTest extends DocTestBase {

    @Override
    List<Class<?>> procedures() {
        return Arrays.asList(
            LinkPredictionPipelineCreateProc.class,
            LinkPredictionPipelineMutateProc.class,
            LinkPredictionPipelineTrainProc.class,
            LinkPredictionPipelineAddStepProcs.class,
            LinkPredictionPipelineConfigureSplitProc.class,
            LinkPredictionPipelineConfigureParamsProc.class,
            GraphCreateProc.class
        );
    }

    @Override
    String adocFile() {
        return "algorithms/alpha/linkprediction-pipeline/linkprediction.adoc";
    }

}