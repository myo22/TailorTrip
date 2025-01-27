package com.tailorTrip.dto;

import com.tailorTrip.domain.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    public String username;
    public String email;
    private Set<MemberRole> roles;
}
