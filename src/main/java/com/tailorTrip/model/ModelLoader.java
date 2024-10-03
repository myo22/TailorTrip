package com.tailorTrip.model;

import com.tailorTrip.initializer.DatasetCreator;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ModelLoader implements CommandLineRunner {

    @Autowired
    private RecommendationModel recommendationModel;

    @Autowired
    private ModelSaver modelSaver;

    @Autowired
    private DatasetCreator datasetCreator;

    @Override
    public void run(String... args) throws Exception {
        String modelPath = "model.zip";
        File modelFile = new File(modelPath);
        if (modelFile.exists()) {
            MultiLayerNetwork loadedModel = modelSaver.loadModel(modelPath);
            recommendationModel.setModel(loadedModel);
        } else {
            DataSetIterator trainData = datasetCreator.createTrainingData();
            recommendationModel.trainModel(trainData);
            modelSaver.saveModel(recommendationModel.getModel(), modelPath);
        }
    }
}
