package com.nhnacademy.gateway.service.impl;

import com.nhnacademy.gateway.dto.basic.AccountDto;
import com.nhnacademy.gateway.dto.create.AccountRegisterRequest;
import com.nhnacademy.gateway.dto.login.AccountLoginRequest;
import com.nhnacademy.gateway.dto.login.AccountLoginResponse;
import com.nhnacademy.gateway.dto.update.AccountUpdateRequest;
import com.nhnacademy.gateway.dto.update.PasswordUpdateRequest;
import com.nhnacademy.gateway.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
//@Profile("!test")
public class AccountServiceImpl implements AccountService {
    private final RestTemplate restTemplate;
    private final RestTemplate authenticatedRestTemplate;

    @Value("${api.account.url}")
    private String accountApiBaseUrl;

    public AccountServiceImpl(@Qualifier("accountRestTemplate") RestTemplate restTemplate,
                              @Qualifier("authenticatedRestTemplate")RestTemplate authenticatedRestTemplate) {
        this.restTemplate = restTemplate;
        this.authenticatedRestTemplate = authenticatedRestTemplate;
    }

    @Override
    public boolean registerAccount(AccountRegisterRequest request){
        String url = accountApiBaseUrl + "/users/register";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AccountRegisterRequest> httpEntity = new HttpEntity<>(request, headers);

        try{
            ResponseEntity<Void> response = restTemplate.postForEntity(url, httpEntity, Void.class);
            return response.getStatusCode() == HttpStatus.CREATED;

        }catch(HttpClientErrorException.Conflict ex){
            log.warn("Account registration failed (Conflict): {}", request.getUsername());
            throw new IllegalArgumentException("이미 사용 중인 아이디 또는 이메일 입니다.");
        }catch(Exception ex){
            log.error("Account API communication failed during registration: {}", ex.getMessage());
            throw new RuntimeException("회원가입 중 서버 오류가 발생했습니다.");
        }
    }

    @Override
    public AccountLoginResponse loginAccount(AccountLoginRequest request) {
        String url = accountApiBaseUrl +"/users/login";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AccountLoginRequest> httpEntity = new HttpEntity<>(request, headers);

        try{
            ResponseEntity<AccountLoginResponse> response = restTemplate.postForEntity(url, httpEntity, AccountLoginResponse.class);
            if(response.getStatusCode()==HttpStatus.OK && response.getBody() != null){
                return response.getBody();
            }
            throw new IllegalArgumentException("로그인 실패: Account-API 응답 오류");
        }catch(HttpClientErrorException ex){
            log.warn("Account login failed (client error): {}", ex.getStatusCode());
            throw new IllegalArgumentException("로그인 실패: 아이디 또는 비밀번호 확인");
        }catch(Exception ex){
            log.error("Account API communication failed during login: {}", ex.getMessage());
            throw new RuntimeException("로그인 중 서버오류 발생");
        }
    }

    @Override
    public AccountDto getAccountDetails(String userId) {
        String url = accountApiBaseUrl + "/users/"+userId;

        try{
            return restTemplate.getForObject(url, AccountDto.class);

        }catch (HttpClientErrorException.NotFound ex){
            log.warn("Account not found: {}", userId);
            throw ex;
        }catch (Exception e){
            log.error("Error fetching account details: {}", e.getMessage());
            throw new RuntimeException("회원 정보 조회 중 서버 오류 발생", e);
        }
    }


    @Override
    public List<AccountDto> getAccountDetailsByIds(List<Long> userIds) {
        if(userIds==null || userIds.isEmpty()){
            return List.of();
        }

        String url = UriComponentsBuilder.fromUriString(accountApiBaseUrl +"/users/batch")
                .queryParam("ids", userIds.stream().map(String::valueOf).collect(Collectors.joining(",")))
                .toUriString();

        try{
            ParameterizedTypeReference<List<AccountDto>> responseType = new ParameterizedTypeReference<List<AccountDto>>() {
            };
            ResponseEntity<List<AccountDto>> response = restTemplate.exchange(url, HttpMethod.GET, null, responseType);
            return response.getBody();
        }catch (Exception e){
            log.error("Error fetching batch account details: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public void updateAccount(String userId, AccountUpdateRequest request) {
        String url = accountApiBaseUrl + "/users/"+userId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AccountUpdateRequest> httpEntity = new HttpEntity<>(request, headers);

        try{
           authenticatedRestTemplate.exchange(url, HttpMethod.PUT, httpEntity, Void.class);
        }catch (HttpClientErrorException ex){
            log.warn("Account update Failed(Client Error{}): {}", ex.getStatusCode(), userId);
            throw new IllegalArgumentException("정보 수정에 실패했습니다.: "+ ex.getMessage());
        }catch (Exception ex){
            log.error("Account API communication failed during update: {}", ex.getMessage());
            throw new RuntimeException("정보 수정 중 서버 오류가 발생했습니다.");
        }
    }

    @Override
    public void updatePassword(String userId, PasswordUpdateRequest request) {
        String url = accountApiBaseUrl + "/users/" + userId + "/password";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PasswordUpdateRequest> httpEntity = new HttpEntity<>(request, headers);

        try{
            authenticatedRestTemplate.exchange(url, HttpMethod.PUT, httpEntity, Void.class);
        }catch (HttpClientErrorException ex){
            log.warn("Password update failed (Client Error {}): {}", ex.getStatusCode(), userId);
            throw new IllegalArgumentException("비밀번호 변경에 실패했습니다 : "+ ex.getMessage());
        }catch (Exception ex){
            log.error("Account API 와 통신 실패 (패스워드 변경동안)");
            throw new RuntimeException("비밀번호 변경 중 서버오류 발생.");
        }
    }

    @Override
    public void deleteAccount(String userId) {
        String url = accountApiBaseUrl + "/users/"+userId;

        try{
            authenticatedRestTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
        }catch (HttpClientErrorException ex){
            log.warn("계정 삭제 실패 (Client error {}): {}", ex.getStatusCode(), userId);
            throw new IllegalArgumentException("회원탈퇴 실패: "+ ex.getMessage());
        }catch (Exception e){
            log.error("Account API와 통신 실패 (삭제하는 동안): {}", e.getMessage());
            throw new RuntimeException("회원 탈퇴 중 서버 오류가 발생했습니다.");
        }
    }

    @Override
    public List<AccountDto> searchAccounts(String query) {
        if(query==null || query.isBlank()){
            return List.of();
        }
        String url = UriComponentsBuilder.fromUriString(accountApiBaseUrl+"/users")
                .queryParam("query", query)
                .toUriString();

        try{
            ParameterizedTypeReference<List<AccountDto>> responseType = new ParameterizedTypeReference<List<AccountDto>>() {};
            ResponseEntity<List<AccountDto>> responseEntity =restTemplate.exchange(url, HttpMethod.GET, null, responseType);
            return responseEntity.getBody() != null ? responseEntity.getBody() : List.of();
        }catch (Exception e){
            log.error("Error searching accounts: {}", e.getMessage());
            return List.of();
        }
    }


}
