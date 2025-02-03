package com.tailorTrip.dto;

import com.tailorTrip.domain.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
    private String username;  // 사용자 고유 ID
    private String email; // 사용자 이메일
    private boolean del; // 삭제된 사용자 여부
    private boolean social; // 소셜 로그인 여부
}
