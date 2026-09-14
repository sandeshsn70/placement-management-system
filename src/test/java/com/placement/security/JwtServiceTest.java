package com.placement.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret",
                "ThisIsADemoJwtSecretKeyForPlacementManagementSystemChangeMeInProduction123456");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L);
    }

    @Test
    void generateToken_andExtractUsername_roundTripsCorrectly() {
        UserDetails userDetails = User.withUsername("asha.rao").password("secret").authorities("ROLE_STUDENT").build();

        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertEquals("asha.rao", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }
}
