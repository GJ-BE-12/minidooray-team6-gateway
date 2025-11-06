package com.nhnacademy.gateway.service.test;

import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Profile("test") // "test" 프로필일 때만 이 빈을 사용
public class TestUserAuthDetailService implements UserDetailsService {

    // SecurityConfig의 PasswordEncoder가 BCrypt이므로 맞춰서 생성
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final String testUserPassword = passwordEncoder.encode("password"); // 테스트 비번 "password"

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if ("testuser".equals(username)) {
            // "testuser"라는 ID로 로그인을 시도하면
            // Account-API를 호출하는 대신, 하드코딩된 User 객체를 반환
            return User.builder()
                    .username("testuser")
                    .password(testUserPassword) // 비번은 "password"
                    .roles("USER", "ADMIN") // 권한은 USER, ADMIN
                    .build();
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }
}