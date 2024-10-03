package com.tailorTrip.model;

import com.tailorTrip.initializer.DatasetCreator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ModelTrainer implements CommandLineRunner {

    @Autowired
    private RecommendationModel recommendationModel;

    @Autowired
    private DatasetCreator datasetCreator;

    @Autowired
    private ModelSaver modelSaver;

    @Override
    public void run(String... args) throws Exception {
        DataSetIterator trainData = datasetCreator.createTrainingData();
        recommendationModel.trainModel(trainData);
        modelSaver.saveModel(recommendationModel.getModel(), "model.zip");
    }
}
