package com.placement.dto.auth;

import com.placement.entity.AccountStatus;
import com.placement.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    // Null when registration created a PENDING student account: there is
    // nothing to authenticate with until a staff member approves it.
    private String token;
    private String username;
    private Role role;
    private AccountStatus status;
    private String message;
}
