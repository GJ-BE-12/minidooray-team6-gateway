package com.nhnacademy.gateway.service;

import com.nhnacademy.gateway.dto.AccountRegisterRequest;

public interface AccountService {
    boolean registerAccount(AccountRegisterRequest request);
}
