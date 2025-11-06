package com.nhnacademy.gateway.service.impl;

import com.nhnacademy.gateway.dto.*;
import com.nhnacademy.gateway.service.DataAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@Profile("!test")
public class DataAggregationServiceImpl implements DataAggregationService {
    private final RestTemplate accountRestTemplate;
    private final RestTemplate taskRestTemplate;

    @Value("${api.account.url}")
    private String accountApiBaseUrl;

    @Value("${api.task.url}")
    private String taskApiBaseUrl;

    public DataAggregationServiceImpl(@Qualifier("accountRestTemplate") RestTemplate accountRestTemplate, @Qualifier("authenticatedRestTemplate") RestTemplate taskRestTemplate) {
        this.accountRestTemplate = accountRestTemplate;
        this.taskRestTemplate = taskRestTemplate;
    }

    @Override
    public Map<String, Object> getDashboardData(String userId){
        Map<String, Object> data = new ConcurrentHashMap<>();

        try{
            String accountUrl = accountApiBaseUrl + "/api/v1/users/{userId}";
            AccountDto accountInfo = accountRestTemplate.getForObject(accountUrl, AccountDto.class, userId);
            if(accountInfo != null){
                data.put("account", accountInfo);
            }else{
                data.put("accountError", "회원 정보를 불러왔으나 데이터가 비어있습니다.");
            }
        }catch(Exception e){
            data.put("accountError", "회원 정보를 불러오는 데 실패했습니다.");
            log.error("Account API Call Error ({}): {}", userId, e.getMessage());
        }

        try{
            String taskUrl = taskApiBaseUrl + "/api/v1/projects/my-list";
            ParameterizedTypeReference<List<TaskProjectDto>> typeRef = new ParameterizedTypeReference<>(){}; // 타입 보존을 위해 사용

            List<TaskProjectDto> projects = taskRestTemplate.exchange(
                    taskUrl,
                    HttpMethod.GET,
                    null,
                    typeRef
            ).getBody();
            data.put("projects", projects != null ? projects : List.of());
        }catch(Exception e){
            data.put("projectError", "프로젝트 목록을 불러오는 데 실패했습니다.");
            log.error("Task API Call Error ({}): {}", userId, e.getMessage());
        }
        return data;
    }

    @Override
    public ProjectDetailsDto getProjectDetails(Long projectId) {
        try{
            String url = taskApiBaseUrl + "/api/v1/projects/"+ projectId;
            return taskRestTemplate.getForObject(url, ProjectDetailsDto.class);
        }catch(Exception e){
            log.error("프로젝트에 대한 프로젝트 디테일을 가져오는데 실패했습니다 {}: {}", projectId,e.getMessage());
            throw new RuntimeException("프로젝트 상세 정보 조회에 실패했습니다.");
        }
    }

    @Override
    public ProjectDto createProject(ProjectCreateRequest request, String adminUserId) {
        try{
            String url = taskApiBaseUrl + "/api/v1/projects";
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProjectCreateRequest> httpEntity = new HttpEntity<>(request, httpHeaders);

            return taskRestTemplate.postForObject(url, httpEntity, ProjectDto.class);
        }catch (Exception e){
            log.error("프로젝트 생성에 실패했습니다. {}:{}", request.getName(), e.getMessage());
            throw new RuntimeException("프로젝트 생성에 실패했습니다.",e);
        }
    }
}
