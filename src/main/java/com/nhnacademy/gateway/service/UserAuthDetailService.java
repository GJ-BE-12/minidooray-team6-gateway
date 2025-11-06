package com.nhnacademy.gateway.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Profile("!test")
public class UserAuthDetailService implements UserDetailsService {

    private final RestTemplate restTemplate;

    @Value("${api.account.url}")
    private String accountApiBaseUrl;


    public UserAuthDetailService(@Qualifier("accountRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //AccountAPI에 인증 정보 요청시 필요한 url
        String authUrl = accountApiBaseUrl + "/api/v1/users/{username}/auth-info";

        //AccountAPI의 인증 api설계에 따라 요청 및 응답 구조를 맞춰야함
        AccountAuthResponse authResponse;

        try{
            authResponse = restTemplate.getForObject(authUrl, AccountAuthResponse.class, username);

            if(authResponse==null){
                throw new UsernameNotFoundException("Authentication Failed: AccountApi returned null for user: "+ username);
            }
        }catch(HttpClientErrorException.NotFound ex){
            throw new UsernameNotFoundException("User not Found: "+ username);
        }catch (Exception ex){
            log.error("AccountApi와 연결 실패"+ ex.getMessage());
            throw new RuntimeException("AccountApi와 연결 실패", ex);
        }

        // 응답 받은 정보를 spring security의 userDetails 객체로 변환 User.builder 자체에서 email을 직접 저장하지 않지만, 나중에 세션에서 상세 정보를 사용할 때 AccountAuthResponse 객체를 따로 저장


        List<String> roles = authResponse.getRoles() != null ? authResponse.getRoles().stream()
                .map(role -> "ROLE_" + role.toUpperCase())
                .collect(Collectors.toList()): Collections.singletonList("ROLE_USER");

        return User.builder()
                .username(authResponse.getUserId())
                .password(authResponse.getEncodedPassword())
                .roles(roles.toArray(new String[0]))
                .build();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccountAuthResponse{
        private String userId;
        private String encodedPassword;
        private String email;
        private List<String> roles;
    }
}
