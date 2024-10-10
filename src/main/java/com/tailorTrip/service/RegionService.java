package com.tailorTrip.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RegionService
{
    private static final Map<String, double[]> REGION_COORDINATES = new HashMap<>();

    static {
        REGION_COORDINATES.put("Seoul", new double[]{37.5665, 126.9780});
        REGION_COORDINATES.put("Daegu", new double[]{35.8722, 128.6025});
        REGION_COORDINATES.put("Busan", new double[]{35.1796, 129.0756});
        REGION_COORDINATES.put("Incheon", new double[]{37.4563, 126.7052});
        REGION_COORDINATES.put("Gwangju", new double[]{35.1595, 126.8526});
        REGION_COORDINATES.put("Daejeon", new double[]{36.3510, 127.3850});
        REGION_COORDINATES.put("Ulsan", new double[]{35.5396, 129.3114});
        REGION_COORDINATES.put("Sejong", new double[]{36.4800, 127.2892});
        REGION_COORDINATES.put("Gangwon", new double[]{37.8228, 128.1555});
        REGION_COORDINATES.put("Chungbuk", new double[]{36.6356, 127.4913});
        REGION_COORDINATES.put("Jeonnam", new double[]{34.8671, 126.9911});
        REGION_COORDINATES.put("Gyeongbuk", new double[]{36.0726, 128.6075});
        REGION_COORDINATES.put("Gyeongnam", new double[]{35.1796, 128.0955});
        REGION_COORDINATES.put("Jeju", new double[]{33.4996, 126.5312});
    }

    public double[] getCoordinates(String region) {
        return REGION_COORDINATES.get(region);
    }

}
