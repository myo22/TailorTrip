package com.tailorTrip.ml;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Component;

@Component
public class RecommendationModel {

    private MultiLayerNetwork model;

    public RecommendationModel() {
        buildModel();
    }

    private void buildModel() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.001))
                .list()
                .layer(new DenseLayer.Builder().nIn(6).nOut(10)
                        .activation(Activation.RELU)
                        .build())
                .layer(new DenseLayer.Builder().nIn(10).nOut(10)
                        .activation(Activation.RELU)
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nIn(10).nOut(3).build()) // 예: 3개의 추천 장소 클래스
                .build();

        model = new MultiLayerNetwork(conf);
        model.init();
        model.setListeners(new ScoreIterationListener(100));

    }

    public void trainModel(DataSetIterator trainingData) {
        model.fit(trainingData);
    }

    public int predict(INDArray input) {
        INDArray output = model.output(input);
        return Nd4j.argMax(output, 1).getInt(0);
    }

    public MultiLayerNetwork getModel() {
        return model;
    }

    public void setModel(MultiLayerNetwork model) {
        this.model = model;
    }
}
