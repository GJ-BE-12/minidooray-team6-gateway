//package com.nhnacademy.gateway.service.test;
//
//import com.nhnacademy.gateway.dto.create.AccountRegisterRequest;
//import com.nhnacademy.gateway.service.AccountService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Service;
//
//@Service
//@Profile("test")
//@Slf4j
//public class TestAccountService implements AccountService {
//
//    // "test" 프로필에서는 RestTemplate이 필요 없으므로 기본 생성자 사용
//    public TestAccountService() {
//    }
//
//    @Override
//    public boolean registerAccount(AccountRegisterRequest request) {
//        // Account-API를 호출하는 대신, 로그만 찍고 무조건 true 반환
//        log.warn("TEST MODE: Faking account registration for user: {}", request.getUsername());
//
//        return true; // 무조건 회원가입 성공 처리
//    }
//
//    @Override
//    public boolean loginAccount(AccountRegisterRequest request) {
//        return false;
//    }
//}