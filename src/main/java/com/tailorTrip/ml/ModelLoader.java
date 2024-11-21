package com.tailorTrip.ml;

import lombok.RequiredArgsConstructor;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ModelLoader implements CommandLineRunner {

    private final RecommendationModel recommendationModel;
    private final ModelSaver modelSaver;
    private final DatasetCreator datasetCreator;

    @Override
    public void run(String... args) throws Exception {
        try {
            // 기존 모델 로드 시도
            MultiLayerNetwork loadedModel = modelSaver.loadModel();
            recommendationModel.setModel(loadedModel);
            System.out.println("모델을 성공적으로 로드했습니다.");
        } catch (IOException e) {
            // 모델 로드 실패 시 학습 시도
            System.out.println("모델 파일이 존재하지 않거나 로드에 실패했습니다. 초기 모델을 생성하고 학습을 시작합니다.");
            try {

                // 학습 데이터 생성
                DataSetIterator trainData = datasetCreator.createTrainingData();

                // 학습 데이터 유효성 검사
                int totalExamples = validateTrainingData(trainData);

                if (totalExamples == 0) {
                    throw new IllegalStateException("훈련 데이터셋이 비어 있습니다. 초기 모델을 생성할 수 없습니다.");
                }

                // 초기 모델 학습
                recommendationModel.initializeModel(); // 모델 초기화
                recommendationModel.trainModel(trainData); // 학습 수행
                System.out.println("초기 모델 학습 성공");


                // 모델 저장
                modelSaver.saveModel(recommendationModel.getModel());
                System.out.println("초기 학습된 모델을 저장했습니다.");
            } catch (Exception ex) {
                System.out.println("모델 학습 또는 저장 중 오류 발생: " + ex.getMessage());
                ex.printStackTrace();
                throw ex; // 애플리케이션 중단 또는 다른 핸들링
            }
        }
    }

    /**
     * 학습 데이터 유효성 검사
     */
    private int validateTrainingData(DataSetIterator trainData) {
        int totalExamples = 0;
        while (trainData.hasNext()) {
            totalExamples += trainData.next().numExamples(); // 배치 크기를 더함
        }
        trainData.reset(); // Iterator 초기화
        System.out.println("훈련 데이터셋의 총 샘플 수: " + totalExamples);
        return totalExamples;
    }
}