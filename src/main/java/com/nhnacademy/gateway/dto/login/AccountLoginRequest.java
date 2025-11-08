package com.nhnacademy.gateway.dto.login;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountLoginRequest {
    private String username;
    private String password;
}
