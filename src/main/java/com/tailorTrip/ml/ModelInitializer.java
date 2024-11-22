package com.tailorTrip.ml;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ModelInitializer implements CommandLineRunner {

    private final RecommendationModel recommendationModel;
    private final ModelSaver modelSaver;
    private final DatasetCreator datasetCreator;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1); // 1개의 스레드 풀만 생성

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
            initializeAndTrainModel();
        }
        // 지속적인 학습 주기 시작 (미세조정 없이)
        startContinuousTraining(); // 새로운 데이터가 들어오면 계속 학습하도록 설정
    }

    private void initializeAndTrainModel() {
        try {
            // 학습 데이터 생성
            DataSetIterator trainData = datasetCreator.createTrainingData();
            recommendationModel.initializeModel(); // 모델 초기화
            recommendationModel.trainModel(trainData); // 학습 수행
            System.out.println("초기 모델 학습 성공");

            // 모델 저장
            modelSaver.saveModel(recommendationModel.getModel());
            System.out.println("초기 학습된 모델을 저장했습니다.");
        } catch (Exception ex) {
            System.out.println("모델 학습 또는 저장 중 오류 발생: " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("모델 학습 또는 저장 중 오류 발생", ex);
        }
    }

    // 새로운 데이터가 추가될 때마다 학습하는 함수
    private void startContinuousTraining() {
        // 데이터 변경 감지 및 실시간 학습 트리거 로직
        // 예시로 10분 간격으로 새 데이터를 학습하는 방식
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // 새로운 데이터를 학습
                DataSetIterator newTrainData = datasetCreator.createTrainingData();
                recommendationModel.trainModel(newTrainData); // 모델 학습
                modelSaver.saveModel(recommendationModel.getModel()); // 모델 저장
                System.out.println("새로운 데이터로 모델을 학습하고 저장했습니다.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 10, TimeUnit.MINUTES); // 10분마다 학습
    }

    @PreDestroy
    public void stopTraining() {
        scheduler.shutdown(); // 애플리케이션 종료 시 스케줄러 종료
        System.out.println("스케줄러 종료");
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