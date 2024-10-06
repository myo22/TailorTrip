package com.tailorTrip.model;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class ModelSaver {

    @Value("${model.path}")
    private String modelPath;

    public void saveModel(MultiLayerNetwork model) throws IOException {
        File file = new File(modelPath);
        // 모델을 저장할 디렉토리가 존재하지 않으면 생성
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        ModelSerializer.writeModel(model, file, true);
    }

    public MultiLayerNetwork loadModel() throws IOException {
        File file = new File(modelPath);
        if (file.exists()) {
            return ModelSerializer.restoreMultiLayerNetwork(file);
        } else {
            throw new IOException("모델 파일이 존재하지 않습니다: " + modelPath);
        }
    }
}
