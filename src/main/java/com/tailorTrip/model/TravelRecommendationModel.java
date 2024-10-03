package com.tailorTrip.model;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

public class TravelRecommendationModel {

    public MultiLayerNetwork createModel(){
        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
                .updater(new Adam(0.001))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(inputSize).nOut(hiddenSize)
                        .activation(Activation.RELU).build())
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nIn(hiddenSize).nOut(outputSize).build())
                .build();

        MultiLayerNetwork model = new MultiLayerNetwork(configuration);
        model.init();
        model.setListeners(new ScoreIterationListener(100));

        return model;
    }

    public void trainModel(DataSetIterator trainingData) {
        MultiLayerNetwork model = createModel();
        model.fit(trainingData);
    }
}
