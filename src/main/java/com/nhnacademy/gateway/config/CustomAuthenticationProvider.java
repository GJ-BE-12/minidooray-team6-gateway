package com.nhnacademy.gateway.config;

import com.nhnacademy.gateway.dto.login.AccountLoginRequest;
import com.nhnacademy.gateway.dto.login.AccountLoginResponse;
import com.nhnacademy.gateway.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final AccountService accountService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        try{
            AccountLoginResponse loginResponse = accountService.loginAccount(new AccountLoginRequest(username,password));

            CustomUserPrincipal principal = new CustomUserPrincipal(
                    loginResponse.getUsername(),
                    null,
                    loginResponse.getId(),
                    Collections.emptyList()
            );


            return new UsernamePasswordAuthenticationToken(principal,
                    null, // 비밀번호는 null 처리
                    Collections.emptyList()); // Authorities 권한
        }catch (IllegalArgumentException | HttpClientErrorException ex){
            throw new BadCredentialsException("로그인 정보가 유효하지 않습니다. ", ex);
        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
