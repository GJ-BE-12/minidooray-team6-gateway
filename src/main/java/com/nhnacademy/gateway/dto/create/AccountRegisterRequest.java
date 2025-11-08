package com.nhnacademy.gateway.dto.create;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

/**
 * 회원가입 시 사용자의 입력 데이터를 담아 accountApi에 전달하는 용도
 * 필드 (username, email, password)
 * id, status, 생성 및 로그인 시간은 서버에서 관리
 */
public class AccountRegisterRequest {
    private String username;
    private String email;
    private String password;
}
