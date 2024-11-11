package com.tailorTrip.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RegionService
{
    private static final Map<String, double[]> REGION_COORDINATES = new HashMap<>();

    static {
        REGION_COORDINATES.put("1", new double[]{37.5665, 126.9780});
        REGION_COORDINATES.put("2", new double[]{37.4563, 126.7052});
        REGION_COORDINATES.put("3", new double[]{36.3510, 127.3850});
        REGION_COORDINATES.put("4", new double[]{35.8722, 128.6025});
        REGION_COORDINATES.put("5", new double[]{35.1595, 126.8526});
        REGION_COORDINATES.put("6", new double[]{35.1796, 129.0756});
        REGION_COORDINATES.put("7", new double[]{35.5396, 129.3114});
        REGION_COORDINATES.put("8", new double[]{36.4800, 127.2892});
        REGION_COORDINATES.put("31", new double[]{37.4138, 127.5183});
        REGION_COORDINATES.put("32", new double[]{37.8228, 128.1555});
        REGION_COORDINATES.put("33", new double[]{36.6356, 127.4913});
        REGION_COORDINATES.put("34", new double[]{36.6355, 126.6520});
        REGION_COORDINATES.put("35", new double[]{36.0726, 128.6075});
        REGION_COORDINATES.put("36", new double[]{35.1796, 128.0955});
        REGION_COORDINATES.put("37", new double[]{35.7193, 127.1574});
        REGION_COORDINATES.put("38", new double[]{34.8671, 126.9911});
        REGION_COORDINATES.put("39", new double[]{33.4996, 126.5312});
    }

    public double[] getCoordinates(String region) {
        return REGION_COORDINATES.get(region);
    }

}
