package com.nhnacademy.gateway.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountLoginResponse {
    private Long id;
    private String username; //-> account-api가 반환해줄 사용자 Id
}
