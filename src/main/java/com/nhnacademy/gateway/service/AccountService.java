package com.nhnacademy.gateway.service;

import com.nhnacademy.gateway.dto.basic.AccountDto;
import com.nhnacademy.gateway.dto.create.AccountRegisterRequest;
import com.nhnacademy.gateway.dto.login.AccountLoginRequest;
import com.nhnacademy.gateway.dto.update.AccountUpdateRequest;
import com.nhnacademy.gateway.dto.update.PasswordUpdateRequest;

public interface AccountService {
    boolean registerAccount(AccountRegisterRequest request);
    String loginAccount(AccountLoginRequest request);
    AccountDto getAccountDetails(String userId);
    void updateAccount(String userId, AccountUpdateRequest request);
    void updatePassword(String userId, PasswordUpdateRequest request);
    void deleteAccount(String userId);

}
