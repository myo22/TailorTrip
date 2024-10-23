package com.tailorTrip.service;

import com.tailorTrip.domain.DetailInfo;

import java.util.List;
import java.util.Map;

public interface KorService {

    String getOverview(Integer contentId, Integer contentTypeId);
    List<DetailInfo> getIntro(Integer contentId, Integer contentTypeId);
    Map<String, String> getDetailInfo(Integer contentId, Integer contentTypeId);
}
