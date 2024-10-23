package com.tailorTrip.service;

import com.tailorTrip.domain.DetailInfo;

import java.util.List;
import java.util.Map;

public interface KorService {

    String getOverview(int contentId, int contentTypeId);
    Map<String, Object> getIntro(Integer contentId, Integer contentTypeId);
    List<DetailInfo> getDetailInfo(Integer contentId, Integer contentTypeId);
}
