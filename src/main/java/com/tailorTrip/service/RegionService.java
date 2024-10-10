package com.tailorTrip.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RegionService
{
    private static final Map<String, double[]> REGION_COORDINATES = new HashMap<>();

    static {
        REGION_COORDINATES.put("서울", new double[]{37.5665, 126.9780});
        REGION_COORDINATES.put("대구", new double[]{35.8722, 128.6025});
        REGION_COORDINATES.put("부산", new double[]{35.1796, 129.0756});
        REGION_COORDINATES.put("인천", new double[]{37.4563, 126.7052});
        REGION_COORDINATES.put("광주", new double[]{35.1595, 126.8526});
        REGION_COORDINATES.put("대전", new double[]{36.3510, 127.3850});
        REGION_COORDINATES.put("울산", new double[]{35.5396, 129.3114});
        REGION_COORDINATES.put("세종", new double[]{36.4800, 127.2892});
        REGION_COORDINATES.put("강원", new double[]{37.8228, 128.1555});
        REGION_COORDINATES.put("충북", new double[]{36.6356, 127.4913});
        REGION_COORDINATES.put("전남", new double[]{34.8671, 126.9911});
        REGION_COORDINATES.put("경북", new double[]{36.0726, 128.6075});
        REGION_COORDINATES.put("경남", new double[]{35.1796, 128.0955});
        REGION_COORDINATES.put("제주", new double[]{33.4996, 126.5312});
    }

    public double[] getCoordinates(String region) {
        return REGION_COORDINATES.get(region);
    }

}
