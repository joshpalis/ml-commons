/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 *
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 *
 */

package org.opensearch.ml.engine;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensearch.ml.common.dataframe.DataFrame;
import org.opensearch.ml.common.dataframe.Row;
import org.opensearch.ml.common.parameter.MLParameter;
import org.opensearch.ml.common.parameter.MLParameterBuilder;
import org.opensearch.ml.engine.contants.MLAlgoNames;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.opensearch.ml.engine.algorithms.clustering.KMeans.DISTANCE_TYPE;
import static org.opensearch.ml.engine.algorithms.clustering.KMeans.ITERATIONS;
import static org.opensearch.ml.engine.algorithms.clustering.KMeans.K;
import static org.opensearch.ml.engine.algorithms.clustering.KMeans.NUM_THREADS;
import static org.opensearch.ml.engine.algorithms.clustering.KMeans.SEED;
import static org.opensearch.ml.engine.helper.KMeansHelper.constructKMeansDataFrame;
import static org.opensearch.ml.engine.helper.LinearRegressionHelper.constructLinearRegressionPredictionDataFrame;
import static org.opensearch.ml.engine.helper.LinearRegressionHelper.constructLinearRegressionTrainDataFrame;
import static org.opensearch.ml.engine.helper.PMMLModelHelper.constructPMMLModelDataFrame;
import static org.opensearch.ml.engine.helper.PMMLModelHelper.constructExpectedPredictions;
import static org.opensearch.ml.engine.algorithms.regression.LinearRegression.BETA1;
import static org.opensearch.ml.engine.algorithms.regression.LinearRegression.BETA2;
import static org.opensearch.ml.engine.algorithms.regression.LinearRegression.EPSILON;
import static org.opensearch.ml.engine.algorithms.regression.LinearRegression.LEARNING_RATE;
import static org.opensearch.ml.engine.algorithms.regression.LinearRegression.OBJECTIVE;
import static org.opensearch.ml.engine.algorithms.regression.LinearRegression.OPTIMISER;
import static org.opensearch.ml.engine.algorithms.regression.LinearRegression.TARGET;

public class MLEngineTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private Set<String> algoNames = new HashSet<>();
    private final String bodyExample = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI/Pgo8UE1NTCB4bWxucz0iaHR0cDovL3d3dy5kbWcub3JnL1BNTUwtNF80IiB4bWxuczpkYXRhPSJodHRwOi8vanBtbWwub3JnL2pwbW1sLW1vZGVsL0lubGluZVRhYmxlIiB2ZXJzaW9uPSI0LjQiPgoJPEhlYWRlcj4KCQk8QXBwbGljYXRpb24gbmFtZT0iSlBNTUwtU2tMZWFybiIgdmVyc2lvbj0iMS42LjE4Ii8+CgkJPFRpbWVzdGFtcD4yMDIxLTA3LTE5VDIzOjIwOjI4WjwvVGltZXN0YW1wPgoJPC9IZWFkZXI+Cgk8RGF0YURpY3Rpb25hcnk+CgkJPERhdGFGaWVsZCBuYW1lPSJ4MSIgb3B0eXBlPSJjb250aW51b3VzIiBkYXRhVHlwZT0iZmxvYXQiLz4KCTwvRGF0YURpY3Rpb25hcnk+Cgk8VHJhbnNmb3JtYXRpb25EaWN0aW9uYXJ5Lz4KCTxNaW5pbmdNb2RlbCBmdW5jdGlvbk5hbWU9InJlZ3Jlc3Npb24iIGFsZ29yaXRobU5hbWU9InNrbGVhcm4uZW5zZW1ibGUuX2lmb3Jlc3QuSXNvbGF0aW9uRm9yZXN0Ij4KCQk8TWluaW5nU2NoZW1hPgoJCQk8TWluaW5nRmllbGQgbmFtZT0ieDEiLz4KCQk8L01pbmluZ1NjaGVtYT4KCQk8T3V0cHV0PgoJCQk8T3V0cHV0RmllbGQgbmFtZT0icmF3QW5vbWFseVNjb3JlIiBvcHR5cGU9ImNvbnRpbnVvdXMiIGRhdGFUeXBlPSJkb3VibGUiIGlzRmluYWxSZXN1bHQ9ImZhbHNlIi8+CgkJCTxPdXRwdXRGaWVsZCBuYW1lPSJub3JtYWxpemVkQW5vbWFseVNjb3JlIiBvcHR5cGU9ImNvbnRpbnVvdXMiIGRhdGFUeXBlPSJkb3VibGUiIGZlYXR1cmU9InRyYW5zZm9ybWVkVmFsdWUiIGlzRmluYWxSZXN1bHQ9ImZhbHNlIj4KCQkJCTxBcHBseSBmdW5jdGlvbj0iLyI+CgkJCQkJPEZpZWxkUmVmIGZpZWxkPSJyYXdBbm9tYWx5U2NvcmUiLz4KCQkJCQk8Q29uc3RhbnQgZGF0YVR5cGU9ImRvdWJsZSI+Mi4zMjcwMjAwNTIwNDI4NDc8L0NvbnN0YW50PgoJCQkJPC9BcHBseT4KCQkJPC9PdXRwdXRGaWVsZD4KCQkJPE91dHB1dEZpZWxkIG5hbWU9ImRlY2lzaW9uRnVuY3Rpb24iIG9wdHlwZT0iY29udGludW91cyIgZGF0YVR5cGU9ImRvdWJsZSIgZmVhdHVyZT0idHJhbnNmb3JtZWRWYWx1ZSI+CgkJCQk8QXBwbHkgZnVuY3Rpb249Ii0iPgoJCQkJCTxDb25zdGFudCBkYXRhVHlwZT0iZG91YmxlIj4wLjU8L0NvbnN0YW50PgoJCQkJCTxBcHBseSBmdW5jdGlvbj0icG93Ij4KCQkJCQkJPENvbnN0YW50IGRhdGFUeXBlPSJkb3VibGUiPjIuMDwvQ29uc3RhbnQ+CgkJCQkJCTxBcHBseSBmdW5jdGlvbj0iKiI+CgkJCQkJCQk8Q29uc3RhbnQgZGF0YVR5cGU9ImRvdWJsZSI+LTEuMDwvQ29uc3RhbnQ+CgkJCQkJCQk8RmllbGRSZWYgZmllbGQ9Im5vcm1hbGl6ZWRBbm9tYWx5U2NvcmUiLz4KCQkJCQkJPC9BcHBseT4KCQkJCQk8L0FwcGx5PgoJCQkJPC9BcHBseT4KCQkJPC9PdXRwdXRGaWVsZD4KCQkJPE91dHB1dEZpZWxkIG5hbWU9Im91dGxpZXIiIG9wdHlwZT0iY2F0ZWdvcmljYWwiIGRhdGFUeXBlPSJib29sZWFuIiBmZWF0dXJlPSJ0cmFuc2Zvcm1lZFZhbHVlIj4KCQkJCTxBcHBseSBmdW5jdGlvbj0ibGVzc09yRXF1YWwiPgoJCQkJCTxGaWVsZFJlZiBmaWVsZD0iZGVjaXNpb25GdW5jdGlvbiIvPgoJCQkJCTxDb25zdGFudCBkYXRhVHlwZT0iZG91YmxlIj4wLjA8L0NvbnN0YW50PgoJCQkJPC9BcHBseT4KCQkJPC9PdXRwdXRGaWVsZD4KCQk8L091dHB1dD4KCQk8TG9jYWxUcmFuc2Zvcm1hdGlvbnM+CgkJCTxEZXJpdmVkRmllbGQgbmFtZT0iZG91YmxlKHgxKSIgb3B0eXBlPSJjb250aW51b3VzIiBkYXRhVHlwZT0iZG91YmxlIj4KCQkJCTxGaWVsZFJlZiBmaWVsZD0ieDEiLz4KCQkJPC9EZXJpdmVkRmllbGQ+CgkJPC9Mb2NhbFRyYW5zZm9ybWF0aW9ucz4KCQk8U2VnbWVudGF0aW9uIG11bHRpcGxlTW9kZWxNZXRob2Q9ImF2ZXJhZ2UiPgoJCQk8U2VnbWVudCBpZD0iMSI+CgkJCQk8VHJ1ZS8+CgkJCQk8VHJlZU1vZGVsIGZ1bmN0aW9uTmFtZT0icmVncmVzc2lvbiIgbWlzc2luZ1ZhbHVlU3RyYXRlZ3k9Im51bGxQcmVkaWN0aW9uIiBub1RydWVDaGlsZFN0cmF0ZWd5PSJyZXR1cm5MYXN0UHJlZGljdGlvbiI+CgkJCQkJPE1pbmluZ1NjaGVtYT4KCQkJCQkJPE1pbmluZ0ZpZWxkIG5hbWU9ImRvdWJsZSh4MSkiLz4KCQkJCQk8L01pbmluZ1NjaGVtYT4KCQkJCQk8Tm9kZSBzY29yZT0iMS4wIj4KCQkJCQkJPFRydWUvPgoJCQkJCQk8Tm9kZSBzY29yZT0iMy4wIj4KCQkJCQkJCTxTaW1wbGVQcmVkaWNhdGUgZmllbGQ9ImRvdWJsZSh4MSkiIG9wZXJhdG9yPSJsZXNzT3JFcXVhbCIgdmFsdWU9IjIuMjU2NTYwNzQyNTA4ODgxNCIvPgoJCQkJCQkJPE5vZGUgc2NvcmU9IjMuMCI+CgkJCQkJCQkJPFNpbXBsZVByZWRpY2F0ZSBmaWVsZD0iZG91YmxlKHgxKSIgb3BlcmF0b3I9Imxlc3NPckVxdWFsIiB2YWx1ZT0iMS45NjkzMTc0NzU3ODUxODM1Ii8+CgkJCQkJCQk8L05vZGU+CgkJCQkJCTwvTm9kZT4KCQkJCQk8L05vZGU+CgkJCQk8L1RyZWVNb2RlbD4KCQkJPC9TZWdtZW50PgoJCQk8U2VnbWVudCBpZD0iMiI+CgkJCQk8VHJ1ZS8+CgkJCQk8VHJlZU1vZGVsIGZ1bmN0aW9uTmFtZT0icmVncmVzc2lvbiIgbWlzc2luZ1ZhbHVlU3RyYXRlZ3k9Im51bGxQcmVkaWN0aW9uIiBub1RydWVDaGlsZFN0cmF0ZWd5PSJyZXR1cm5MYXN0UHJlZGljdGlvbiI+CgkJCQkJPE1pbmluZ1NjaGVtYT4KCQkJCQkJPE1pbmluZ0ZpZWxkIG5hbWU9ImRvdWJsZSh4MSkiLz4KCQkJCQk8L01pbmluZ1NjaGVtYT4KCQkJCQk8Tm9kZSBzY29yZT0iMi4wIj4KCQkJCQkJPFRydWUvPgoJCQkJCQk8Tm9kZSBzY29yZT0iMi4wIj4KCQkJCQkJCTxTaW1wbGVQcmVkaWNhdGUgZmllbGQ9ImRvdWJsZSh4MSkiIG9wZXJhdG9yPSJsZXNzT3JFcXVhbCIgdmFsdWU9IjEuNDQ0MzAyMjE5MTczMDgwMyIvPgoJCQkJCQk8L05vZGU+CgkJCQkJCTxOb2RlIHNjb3JlPSIzLjAiPgoJCQkJCQkJPFNpbXBsZVByZWRpY2F0ZSBmaWVsZD0iZG91YmxlKHgxKSIgb3BlcmF0b3I9Imxlc3NPckVxdWFsIiB2YWx1ZT0iMi45OTIxMDc2NDkzMzAwOTk4Ii8+CgkJCQkJCTwvTm9kZT4KCQkJCQk8L05vZGU+CgkJCQk8L1RyZWVNb2RlbD4KCQkJPC9TZWdtZW50PgoJCQk8U2VnbWVudCBpZD0iMyI+CgkJCQk8VHJ1ZS8+CgkJCQk8VHJlZU1vZGVsIGZ1bmN0aW9uTmFtZT0icmVncmVzc2lvbiIgbWlzc2luZ1ZhbHVlU3RyYXRlZ3k9Im51bGxQcmVkaWN0aW9uIiBub1RydWVDaGlsZFN0cmF0ZWd5PSJyZXR1cm5MYXN0UHJlZGljdGlvbiI+CgkJCQkJPE1pbmluZ1NjaGVtYT4KCQkJCQkJPE1pbmluZ0ZpZWxkIG5hbWU9ImRvdWJsZSh4MSkiLz4KCQkJCQk8L01pbmluZ1NjaGVtYT4KCQkJCQk8Tm9kZSBzY29yZT0iMi4wIj4KCQkJCQkJPFRydWUvPgoJCQkJCQk8Tm9kZSBzY29yZT0iMi4wIj4KCQkJCQkJCTxTaW1wbGVQcmVkaWNhdGUgZmllbGQ9ImRvdWJsZSh4MSkiIG9wZXJhdG9yPSJsZXNzT3JFcXVhbCIgdmFsdWU9IjEuNDAxMTYyMDIxOTgwMjMwOCIvPgoJCQkJCQk8L05vZGU+CgkJCQkJCTxOb2RlIHNjb3JlPSIzLjAiPgoJCQkJCQkJPFNpbXBsZVByZWRpY2F0ZSBmaWVsZD0iZG91YmxlKHgxKSIgb3BlcmF0b3I9Imxlc3NPckVxdWFsIiB2YWx1ZT0iMi42MDkwNDIzOTA5OTg5MzgiLz4KCQkJCQkJPC9Ob2RlPgoJCQkJCTwvTm9kZT4KCQkJCTwvVHJlZU1vZGVsPgoJCQk8L1NlZ21lbnQ+CgkJCTxTZWdtZW50IGlkPSI0Ij4KCQkJCTxUcnVlLz4KCQkJCTxUcmVlTW9kZWwgZnVuY3Rpb25OYW1lPSJyZWdyZXNzaW9uIiBtaXNzaW5nVmFsdWVTdHJhdGVneT0ibnVsbFByZWRpY3Rpb24iIG5vVHJ1ZUNoaWxkU3RyYXRlZ3k9InJldHVybkxhc3RQcmVkaWN0aW9uIj4KCQkJCQk8TWluaW5nU2NoZW1hPgoJCQkJCQk8TWluaW5nRmllbGQgbmFtZT0iZG91YmxlKHgxKSIvPgoJCQkJCTwvTWluaW5nU2NoZW1hPgoJCQkJCTxOb2RlIHNjb3JlPSIyLjAiPgoJCQkJCQk8VHJ1ZS8+CgkJCQkJCTxOb2RlIHNjb3JlPSIyLjAiPgoJCQkJCQkJPFNpbXBsZVByZWRpY2F0ZSBmaWVsZD0iZG91YmxlKHgxKSIgb3BlcmF0b3I9Imxlc3NPckVxdWFsIiB2YWx1ZT0iMS4xNTA1NjkxNTQwMTk3MzI1Ii8+CgkJCQkJCTwvTm9kZT4KCQkJCQkJPE5vZGUgc2NvcmU9IjMuMCI+CgkJCQkJCQk8U2ltcGxlUHJlZGljYXRlIGZpZWxkPSJkb3VibGUoeDEpIiBvcGVyYXRvcj0ibGVzc09yRXF1YWwiIHZhbHVlPSIyLjc2OTA0MjIzOTg4MjUzNyIvPgoJCQkJCQk8L05vZGU+CgkJCQkJPC9Ob2RlPgoJCQkJPC9UcmVlTW9kZWw+CgkJCTwvU2VnbWVudD4KCQkJPFNlZ21lbnQgaWQ9IjUiPgoJCQkJPFRydWUvPgoJCQkJPFRyZWVNb2RlbCBmdW5jdGlvbk5hbWU9InJlZ3Jlc3Npb24iIG1pc3NpbmdWYWx1ZVN0cmF0ZWd5PSJudWxsUHJlZGljdGlvbiIgbm9UcnVlQ2hpbGRTdHJhdGVneT0icmV0dXJuTGFzdFByZWRpY3Rpb24iPgoJCQkJCTxNaW5pbmdTY2hlbWE+CgkJCQkJCTxNaW5pbmdGaWVsZCBuYW1lPSJkb3VibGUoeDEpIi8+CgkJCQkJPC9NaW5pbmdTY2hlbWE+CgkJCQkJPE5vZGUgc2NvcmU9IjEuMCI+CgkJCQkJCTxUcnVlLz4KCQkJCQkJPE5vZGUgc2NvcmU9IjMuMCI+CgkJCQkJCQk8U2ltcGxlUHJlZGljYXRlIGZpZWxkPSJkb3VibGUoeDEpIiBvcGVyYXRvcj0ibGVzc09yRXF1YWwiIHZhbHVlPSIyLjIwNjI1NDk5NTA1ODQwOTQiLz4KCQkJCQkJCTxOb2RlIHNjb3JlPSIzLjAiPgoJCQkJCQkJCTxTaW1wbGVQcmVkaWNhdGUgZmllbGQ9ImRvdWJsZSh4MSkiIG9wZXJhdG9yPSJsZXNzT3JFcXVhbCIgdmFsdWU9IjEuMjM4ODg0MTM3MTIzMzAzNSIvPgoJCQkJCQkJPC9Ob2RlPgoJCQkJCQk8L05vZGU+CgkJCQkJPC9Ob2RlPgoJCQkJPC9UcmVlTW9kZWw+CgkJCTwvU2VnbWVudD4KCQk8L1NlZ21lbnRhdGlvbj4KCTwvTWluaW5nTW9kZWw+CjwvUE1NTD4K";

    @Before
    public void setUp() {
        algoNames.add(MLAlgoNames.KMEANS);
        algoNames.add(MLAlgoNames.LINEAR_REGRESSION);
    }

    @Test
    public void predictKMeans() {
        Model model = trainKMeansModel();
        DataFrame predictionDataFrame = constructKMeansDataFrame(10);
        DataFrame predictions = MLEngine.predict("kmeans", null, predictionDataFrame, model);
        Assert.assertEquals(10, predictions.size());
        predictions.forEach(row -> Assert.assertTrue(row.getValue(0).intValue() == 0 || row.getValue(0).intValue() == 1));
    }

    @Test
    public void predictLinearRegression() {
        Model model = trainLinearRegressionModel();
        DataFrame predictionDataFrame = constructLinearRegressionPredictionDataFrame();
        DataFrame predictions = MLEngine.predict("linear_regression", null, predictionDataFrame, model);
        Assert.assertEquals(2, predictions.size());
    }

    @Test
    public void predictPMMLModel() {
        Model model = retrievePMMLModel();
        DataFrame predictionDataFrame = constructPMMLModelDataFrame();
        DataFrame predictions = MLEngine.predict("pmml", null, predictionDataFrame, model);
        DataFrame expectedPredictions = constructExpectedPredictions();
        Assert.assertEquals(4, predictions.size());
        Assert.assertEquals(2, predictions.getRow(0).size());
        for (int i = 0; i < 4; i++) {
            Row expected = expectedPredictions.getRow(i);
            Row prediction = predictions.getRow(i);
            for (int j = 0; j < 2; j++) {
                Assert.assertEquals(expected.getValue(j).getValue(), prediction.getValue(j).getValue());
            }
        }
    }

    @Test
    public void unsupportedTrainPMMLModel() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Unsupported train: PMML custom models");
        MLEngine.train("pmml", null, null);
    }

    @Test
    public void trainKMeans() {
        Model model = trainKMeansModel();
        Assert.assertEquals("KMeans", model.getName());
        Assert.assertEquals(1, model.getVersion());
        Assert.assertNotNull(model.getContent());
    }

    @Test
    public void trainLinearRegression() {
        Model model = trainLinearRegressionModel();
        Assert.assertEquals("LinearRegression", model.getName());
        Assert.assertEquals(1, model.getVersion());
        Assert.assertNotNull(model.getContent());
    }

    @Test
    public void trainUnsupportedAlgorithm() {
        String algoName = "unsupported_algorithm";
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Unsupported algorithm: " + algoName);
        MLEngine.train(algoName, null, null);
    }

    @Test
    public void predictUnsupportedAlgorithm() {
        String algoName = "unsupported_algorithm";
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("Unsupported algorithm: " + algoName);
        MLEngine.predict(algoName, null, null, null);
    }

    @Test
    public void predictWithoutModel() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("No model found for linear regression prediction.");
        MLEngine.predict("linear_regression", null, null, null);
    }

    @Test
    public void getMetaData() {
        MLEngineMetaData metaData = MLEngine.getMetaData();
        metaData.getAlgoMetaDataList().forEach(e -> Assert.assertTrue(algoNames.contains(e.getName())));
    }

    private Model trainKMeansModel() {
        List<MLParameter> parameters = new ArrayList<>();
        parameters.add(MLParameterBuilder.parameter(SEED, 1L));
        parameters.add(MLParameterBuilder.parameter(NUM_THREADS, 1));
        parameters.add(MLParameterBuilder.parameter(DISTANCE_TYPE, 0));
        parameters.add(MLParameterBuilder.parameter(ITERATIONS, 10));
        parameters.add(MLParameterBuilder.parameter(K, 2));
        DataFrame trainDataFrame = constructKMeansDataFrame(100);
        return MLEngine.train("kmeans", parameters, trainDataFrame);
    }

    private Model trainLinearRegressionModel() {
        List<MLParameter> parameters = new ArrayList<>();
        parameters.add(MLParameterBuilder.parameter(OBJECTIVE, 0));
        parameters.add(MLParameterBuilder.parameter(OPTIMISER, 5));
        parameters.add(MLParameterBuilder.parameter(LEARNING_RATE, 0.01));
        parameters.add(MLParameterBuilder.parameter(EPSILON, 1e-6));
        parameters.add(MLParameterBuilder.parameter(BETA1, 0.9));
        parameters.add(MLParameterBuilder.parameter(BETA2, 0.99));
        parameters.add(MLParameterBuilder.parameter(TARGET, "price"));
        DataFrame trainDataFrame = constructLinearRegressionTrainDataFrame();
        return MLEngine.train("linear_regression", parameters, trainDataFrame);
    }

    private Model retrievePMMLModel() {
        Model model = new Model();
        model.setVersion(1);
        model.setName("test");
        model.setFormat("pmml");
        model.setAlgorithm("isolationforest");
        byte[] bodyBytes = Base64.getDecoder().decode(bodyExample);
        model.setContent(bodyBytes);
        return model;
    }
}