package com.tailorTrip.ml;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Component;

import static com.tailorTrip.ml.DataPreprocessor.CATEGORY_MAP;

@Component
public class RecommendationModel {

    private MultiLayerNetwork model;

    private void initializeModel(int inputSize, int outputSize) {
        NeuralNetConfiguration.ListBuilder builder = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.001))
                .list();

        // 첫 번째 은닉층
        builder.layer(0, new DenseLayer.Builder()
                .nIn(inputSize)
                .nOut(128)
                .activation(Activation.RELU)
                .weightInit(WeightInit.XAVIER)
                .build());

        // 두 번째 은닉층
        builder.layer(1, new DenseLayer.Builder()
                .nIn(128)
                .nOut(64)
                .activation(Activation.RELU)
                .weightInit(WeightInit.XAVIER)
                .build());

        // 출력층
        builder.layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT)
                .activation(Activation.SOFTMAX)
                .nIn(64)
                .nOut(outputSize)
                .build());

        model = new MultiLayerNetwork(builder.build());
        model.init();
    }

    // 모델 학습 메소드
    public void trainModel(DataSetIterator trainingData) {
        model.fit(trainingData);
    }

    // 모델 예측 메소드
    public INDArray predict(INDArray input) {
        return model.output(input, false);
    }

    public MultiLayerNetwork getModel() {
        return model;
    }

    public void setModel(MultiLayerNetwork model) {
        this.model = model;
    }
}
