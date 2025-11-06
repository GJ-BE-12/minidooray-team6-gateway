package com.nhnacademy.gateway.service.impl;

import com.nhnacademy.gateway.dto.AccountRegisterRequest;
import com.nhnacademy.gateway.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@Profile("!test")
public class AccountServiceImpl implements AccountService {
    private final RestTemplate restTemplate;

    @Value("${api.account.url}")
    private String accountApiBaseUrl;

    public AccountServiceImpl(@Qualifier("accountRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean registerAccount(AccountRegisterRequest request){
        String url = accountApiBaseUrl + "/members/register";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AccountRegisterRequest> httpEntity = new HttpEntity<>(request, headers);

        try{
            ResponseEntity<Void> response = restTemplate.postForEntity(url, httpEntity, Void.class); //accountAPI에서 201 Created를 반환할 것을 기대 아니면 바꿔야됨
            return response.getStatusCode() == HttpStatus.CREATED;

        }catch(HttpClientErrorException.Conflict ex){
            log.warn("Account registration failed (Conflict): {}", request.getUsername());
            throw new IllegalArgumentException("이미 사용 중인 아이디 또는 이메일 입니다.");
        }catch(Exception ex){
            log.error("Account API communication failed during registration: {}", ex.getMessage());
            throw new RuntimeException("회원가입 중 서버 오류가 발생했습니다.");
        }
    }
}
