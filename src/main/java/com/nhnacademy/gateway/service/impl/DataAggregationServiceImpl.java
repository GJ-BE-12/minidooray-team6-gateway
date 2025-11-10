package com.nhnacademy.gateway.service.impl;

import com.nhnacademy.gateway.dto.basic.AccountDto;
import com.nhnacademy.gateway.dto.basic.ProjectDto;
import com.nhnacademy.gateway.dto.basic.TaskProjectDto;
import com.nhnacademy.gateway.dto.create.ProjectCreateRequest;
import com.nhnacademy.gateway.dto.detail.ProjectDetailsDto;
import com.nhnacademy.gateway.dto.update.ProjectUpdateRequest;
import com.nhnacademy.gateway.service.DataAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
//@Profile("!test")
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
            String accountUrl = accountApiBaseUrl + "/users/{userId}";

            AccountDto accountInfo = taskRestTemplate.getForObject(accountUrl, AccountDto.class, userId);
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
            String taskUrl = taskApiBaseUrl + "/projects";
            ParameterizedTypeReference<List<ProjectDto>> typeRef = new ParameterizedTypeReference<>(){}; // 타입 보존을 위해 사용

            List<ProjectDto> projects = taskRestTemplate.exchange(
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
            String url = taskApiBaseUrl + "/projects/"+ projectId;
            return taskRestTemplate.getForObject(url, ProjectDetailsDto.class);
        }catch(Exception e){
            log.error("프로젝트에 대한 프로젝트 디테일을 가져오는데 실패했습니다 {}: {}", projectId,e.getMessage());
            throw new RuntimeException("프로젝트 상세 정보 조회에 실패했습니다.");
        }
    }


    @Override
    public ProjectCreateRequest createProject(ProjectCreateRequest request) {
        try{
            String url = taskApiBaseUrl + "/projects";
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProjectCreateRequest> httpEntity = new HttpEntity<>(request, httpHeaders);

            return taskRestTemplate.postForObject(url, httpEntity, ProjectCreateRequest.class);
        }catch (Exception e){
            log.error("프로젝트 생성에 실패했습니다. {}:{}", request.getName(), e.getMessage());
            throw new RuntimeException("프로젝트 생성에 실패했습니다.",e);
        }
    }

    @Override
    public ProjectDto getProject(Long projectId) {
        String url = taskApiBaseUrl + "/projects/"+ projectId;

        try{
            return taskRestTemplate.getForObject(url, ProjectDto.class);
        }catch (HttpClientErrorException.NotFound ex){
            log.warn("Project not found: {}", projectId);
            throw ex;
        }catch (Exception e){
            log.error("Error fetching Project: {}", e.getMessage());
            throw new RuntimeException("프로젝트 정보 조회 중 서버 오류 발생", e);
        }
    }

    @Override
    public void updateProject(Long projectId, ProjectUpdateRequest request) {
        String url = taskApiBaseUrl + "/projects/" + projectId;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProjectUpdateRequest> httpEntity = new HttpEntity<>(request, headers);

        try{
            taskRestTemplate.exchange(url, HttpMethod.PUT, httpEntity, Void.class);
        }catch (HttpClientErrorException ex){
            log.warn("Project update Failed(Client Error{}): {}", ex.getStatusCode(), projectId);
            throw new IllegalArgumentException("정보 수정에 실패했습니다.: "+ ex.getMessage());
        }catch (Exception ex){
            log.error("Project API communication failed during update: {}", ex.getMessage());
            throw new RuntimeException("정보 수정 중 서버 오류가 발생했습니다.");
        }
    }

    @Override
    public void deleteProject(Long projectId) {
        String url = taskApiBaseUrl + "/projects/" +projectId;
        try{
            taskRestTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);

            log.info("프로젝트 삭제가 성공적으로 됐습니다 (Task-API): {}", projectId);
        }catch (HttpClientErrorException ex){
            log.warn("Project delete Failed(Client Error{}): {}", ex.getStatusCode(), projectId);
            throw new IllegalArgumentException("프로젝트 삭제에 실패했습니다.: "+ ex.getMessage());
        }catch (Exception ex){
            log.error("Project API communication failed during update: {}", ex.getMessage());
            throw new RuntimeException("프로젝트 삭제 중 서버 오류가 발생했습니다.");
        }
    }
}
