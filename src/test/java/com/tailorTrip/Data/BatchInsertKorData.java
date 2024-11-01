package com.tailorTrip.Data;

import com.tailorTrip.service.KorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
@RequiredArgsConstructor
public class BatchInsertKorData {

    private final KorService korService;

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(BatchInsertKorData.class);
        BatchInsertKorData batchInsertKorData = context.getBean(BatchInsertKorData.class);

        int contentTypeId = 12;
        List<Integer> contentIds = IntStream.rangeClosed(1, 50000)
                .boxed()
                .collect(Collectors.toList());

        batchInsertKorData.batchUpdateOverview(contentIds, contentTypeId);
    }

    public void batchUpdateOverview(List<Integer> contentIds, int contentTypeId) {
        for (Integer contentId : contentIds) {
            String overview = korService.getOverview(contentId, contentTypeId);
            updateOverviewInDatabase(contentId, overview);
        }
        System.out.println("Overview 업데이트 완료!");
    }

    private void updateOverviewInDatabase(int contentId, String overview) {
        String url = "jdbc:mysql://localhost:3306/tailortrip_db";
        String user = "root";
        String password = "220211";
        String updateQuery = "UPDATE place SET overview = ? WHERE content_id = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setString(1, overview);
            pstmt.setInt(2, contentId);
            pstmt.executeUpdate();

            System.out.println("Content ID " + contentId + "에 대한 overview 업데이트 완료.");
        } catch (SQLException e) {
            System.err.println("Content ID " + contentId + "에 대한 업데이트 실패: " + e.getMessage());
        }
    }
}
