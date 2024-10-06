package com.tailorTrip.model;

import com.tailorTrip.initializer.DatasetCreator;
import lombok.RequiredArgsConstructor;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ModelLoader implements CommandLineRunner {

    private final RecommendationModel recommendationModel;

    private final ModelSaver modelSaver;

    private final DatasetCreator datasetCreator;

    @Override
    public void run(String... args) throws Exception {
        try {
            MultiLayerNetwork loadedModel = modelSaver.loadModel();
            recommendationModel.setModel(loadedModel);
            System.out.println("모델을 성공적으로 로드했습니다.");
        } catch (IOException e) {
            System.out.println("모델 파일이 존재하지 않거나 로드에 실패했습니다. 모델을 학습시킵니다.");
            DataSetIterator trainData = datasetCreator.createTrainingData();
            recommendationModel.trainModel(trainData);
            modelSaver.saveModel(recommendationModel.getModel());
            System.out.println("모델을 학습시키고 저장했습니다.");
        }
    }
}
