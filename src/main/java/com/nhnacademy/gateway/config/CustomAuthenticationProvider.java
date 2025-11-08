package com.nhnacademy.gateway.config;

import com.nhnacademy.gateway.dto.login.AccountLoginRequest;
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
            String userId = accountService.loginAccount(new AccountLoginRequest(username,password));

            //인증 성공시, spring Security Context에 저장할 토큰 생성 (Principal에 userId를 넣어야 UserAuthInterceptor 사용이 가능하기 때문에
            //토큰 생성할때 userId를 넣음
            return new UsernamePasswordAuthenticationToken(userId, // Principal (인터셉터가 꺼내 쓸 ID)
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
