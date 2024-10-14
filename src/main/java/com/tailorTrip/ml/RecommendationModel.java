package com.tailorTrip.ml;

import jakarta.annotation.PostConstruct;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;

import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.springframework.stereotype.Component;


@Component
public class RecommendationModel {

    private MultiLayerNetwork model;

    @PostConstruct
    public void initializeModel() {
        int inputSize = 4; // DataPreprocessor에서 정의한 특성 벡터 크기
        int hiddenLayer1Size = 128;
        int hiddenLayer2Size = 64;
        int outputSize = DataPreprocessor.CATEGORY_MAP.size() + DataPreprocessor.SUBCATEGORY_MAP.size() + DataPreprocessor.DETAIL_CATEGORY_MAP.size(); // 6 + 18 + 15 = 39

        NeuralNetConfiguration.ListBuilder builder = new NeuralNetConfiguration.Builder()
                .seed(123)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam(0.001))
                .list();

        // 첫 번째 은닉층
        builder.layer(0, new DenseLayer.Builder()
                .nIn(inputSize)
                .nOut(hiddenLayer1Size)
                .activation(Activation.RELU)
                .weightInit(WeightInit.XAVIER)
                .build());

        // 두 번째 은닉층
        builder.layer(1, new DenseLayer.Builder()
                .nIn(hiddenLayer1Size)
                .nOut(hiddenLayer2Size)
                .activation(Activation.RELU)
                .weightInit(WeightInit.XAVIER)
                .build());

        // 출력층 (Binary Cross-Entropy 손실 함수와 Sigmoid 활성화)
        builder.layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.XENT) // Binary Cross-Entropy
                .activation(Activation.SIGMOID)
                .nIn(hiddenLayer2Size)
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