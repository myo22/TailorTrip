package com.tailorTrip.ml;

import lombok.RequiredArgsConstructor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModelTrainer implements CommandLineRunner {

    private final RecommendationModel recommendationModel;
    private final DatasetCreator datasetCreator;
    private final ModelSaver modelSaver;

    @Override
    public void run(String... args) throws Exception {
        // 학습 데이터 준비
        DataSetIterator trainData = datasetCreator.createTrainingData();

        // 모델 학습
        recommendationModel.trainModel(trainData);
        System.out.println("모델을 학습시켰습니다.");

        // 학습된 모델 저장 (ModelSaver가 application.yml에서 경로를 가져옴)
        modelSaver.saveModel(recommendationModel.getModel());
        System.out.println("모델을 학습시키고 저장했습니다.");
    }
}
