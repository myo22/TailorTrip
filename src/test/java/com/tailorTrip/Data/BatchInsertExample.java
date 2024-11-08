package com.tailorTrip.Data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Paths;

@SpringBootTest
public class BatchInsertExample {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/tailortrip_db";
        String user = "root";
        String password = "220211";
        String insertQuery = "INSERT INTO place (id, title, addr1, addr2, zipcode, mapx, mapy, tel, content_id, cat1, cat2, cat3, acmpy_type_cd, first_image, first_image2, area_code, sigungu_code, content_type_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

            // JSON 파일 읽기
            String jsonData = new String(Files.readAllBytes(Paths.get("src/main/resources/tourist_data.json")));
            JSONArray jsonArray = new JSONArray(jsonData);

            int count = 0;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);

                pstmt.setInt(1, obj.optInt("id", 0));
                pstmt.setString(2, obj.optString("title", ""));
                pstmt.setString(3, obj.optString("addr1", ""));
                pstmt.setString(4, obj.optString("addr2", ""));

                // zipcode 처리: 문자열일 가능성을 고려
                String zipcodeStr = obj.optString("zipcode", "").trim();
                Integer zipcode = null;
                if (!zipcodeStr.isEmpty()) {
                    try {
                        // 숫자 이외의 문자를 제거한 후 파싱
                        String sanitizedZipcode = zipcodeStr.replaceAll("[^0-9]", "");
                        if (!sanitizedZipcode.isEmpty()) {
                            zipcode = Integer.parseInt(sanitizedZipcode);
                        }
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid zipcode for record ID " + obj.optInt("id", 0) + ": " + zipcodeStr);
                    }
                }
                if (zipcode != null) {
                    pstmt.setInt(5, zipcode);
                } else {
                    pstmt.setNull(5, java.sql.Types.INTEGER);
                }

                // mapx
                double mapx = obj.optDouble("mapx", 0.0);
                pstmt.setDouble(6, mapx);

                // mapy
                double mapy = obj.optDouble("mapy", 0.0);
                pstmt.setDouble(7, mapy);

                // tel 처리: 길이 제한 및 유효성 검사
                String tel = obj.optString("tel", "").trim();
                if (tel.length() > 500) { // MySQL 컬럼 길이에 맞게 조정
                    tel = tel.substring(0, 500);
                    System.err.println("Tel too long for record ID " + obj.optInt("id", 0) + ". Truncated to: " + tel);
                }
                pstmt.setString(8, tel);

                // contentid
                pstmt.setInt(9, obj.optInt("contentid", 0));

                // cat1
                pstmt.setString(10, obj.optString("cat1", ""));

                // cat2
                pstmt.setString(11, obj.optString("cat2", ""));

                // cat3
                pstmt.setString(12, obj.optString("cat3", ""));

                // acmpyTypeCd
                pstmt.setString(13, obj.optString("acmpyTypeCd", ""));

                // firstimage
                pstmt.setString(14, obj.optString("firstimage", ""));

                // firstimage2
                pstmt.setString(15, obj.optString("firstimage2", ""));

                // areacode
                pstmt.setString(16, obj.optString("areacode", ""));

                // sigungucode 처리: null 가능성을 고려
                Object sigungucodeObj = obj.opt("sigungucode");
                if (sigungucodeObj != null) {
                    String sigungucodeStr = sigungucodeObj.toString().trim();
                    Integer sigungucode = null;
                    if (!sigungucodeStr.isEmpty()) {
                        try {
                            // 숫자 이외의 문자를 제거한 후 파싱
                            String sanitizedSigungucode = sigungucodeStr.replaceAll("[^0-9]", "");
                            if (!sanitizedSigungucode.isEmpty()) {
                                sigungucode = Integer.parseInt(sigungucodeStr.replaceAll("[^0-9]", ""));
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid sigungucode for record ID " + obj.optInt("id", 0) + ": " + sigungucodeStr);
                        }
                    }
                    if (sigungucode != null) {
                        pstmt.setInt(17, sigungucode);
                    } else {
                        pstmt.setNull(17, java.sql.Types.INTEGER);
                    }
                } else {
                    pstmt.setNull(17, java.sql.Types.INTEGER);
                }

                // contentTypeId
                pstmt.setInt(18, obj.optInt("contentTypeId", 0));

                pstmt.addBatch();

                if (++count % 1000 == 0) { // 1000개씩 배치 실행
                    pstmt.executeBatch();
                    System.out.println(count + " records inserted.");
                }
            }
            pstmt.executeBatch(); // 남은 데이터 처리
            System.out.println("데이터 삽입 완료! 총 " + count + " 레코드 삽입.");

        } catch (SQLException | java.io.IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}