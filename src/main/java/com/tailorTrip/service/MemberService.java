package com.tailorTrip.service;

import com.tailorTrip.domain.Member;
import com.tailorTrip.dto.MemberJoinDTO;

public interface MemberService {

    static class MidExistException extends Exception {


    }

    void join(MemberJoinDTO memberJoinDTO) throws MidExistException;

    Member findMemberById(String userId);
}
