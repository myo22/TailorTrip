package com.tailorTrip.ml;

import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class ModelSaver {

    @Value("${model.path}")
    private String modelPath;

    @Autowired
    private ResourceLoader resourceLoader;

    public void saveModel(MultiLayerNetwork model) throws IOException {
        // modelPath가 classpath일 때 Resource를 사용
        Resource resource = resourceLoader.getResource(modelPath);
        File file = resource.getFile(); // Resource에서 File 객체를 가져옴

        // 모델을 저장할 디렉토리가 존재하지 않으면 생성
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();  // 부모 디렉토리 생성
        } else if (parentDir == null) {
            throw new IOException("잘못된 파일 경로입니다: " + modelPath);
        }

        // 모델 저장
        ModelSerializer.writeModel(model, file, true);
        System.out.println("모델이 " + modelPath + "에 성공적으로 저장되었습니다.");
    }

    public MultiLayerNetwork loadModel() throws IOException {
        Resource resource = resourceLoader.getResource(modelPath);
        File file = resource.getFile(); // Resource에서 File 객체를 가져옴

        if (file.exists()) {
            System.out.println("모델 파일을 " + modelPath + "에서 로드합니다.");
            return ModelSerializer.restoreMultiLayerNetwork(file);
        } else {
            throw new IOException("모델 파일이 존재하지 않습니다: " + modelPath);
        }
    }
}
