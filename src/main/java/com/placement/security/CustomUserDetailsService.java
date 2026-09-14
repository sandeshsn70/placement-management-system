package com.placement.security;

import com.placement.entity.AccountStatus;
import com.placement.entity.User;
import com.placement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // A PENDING account can't authenticate yet (awaiting placement cell /
        // admin approval); a REJECTED one never will. Spring Security's default
        // pre-authentication checks turn these into DisabledException /
        // LockedException respectively, before the password is even checked.
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .disabled(user.getStatus() == AccountStatus.PENDING)
                .accountLocked(user.getStatus() == AccountStatus.REJECTED)
                .build();
    }
}
