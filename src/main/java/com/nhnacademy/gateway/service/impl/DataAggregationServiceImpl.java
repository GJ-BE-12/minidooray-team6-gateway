package com.nhnacademy.gateway.service.impl;

import com.nhnacademy.gateway.dto.basic.AccountDto;
import com.nhnacademy.gateway.dto.basic.ProjectDto;
import com.nhnacademy.gateway.dto.basic.TaskProjectDto;
import com.nhnacademy.gateway.dto.create.*;
import com.nhnacademy.gateway.dto.detail.ProjectDetailsDto;
import com.nhnacademy.gateway.dto.detail.TaskApiResponseDto;
import com.nhnacademy.gateway.dto.detail.TaskDetailsDto;
import com.nhnacademy.gateway.dto.relation.ProjectMemberAddRequest;
import com.nhnacademy.gateway.dto.relation.TaskAddTagRequest;
import com.nhnacademy.gateway.dto.relation.TaskSetMileStoneRequest;
import com.nhnacademy.gateway.dto.update.*;
import com.nhnacademy.gateway.service.AccountService;
import com.nhnacademy.gateway.service.DataAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
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
    private final RestTemplate taskRestTemplate;
    private final AccountService accountService;

    @Value("${api.account.url}")
    private String accountApiBaseUrl;

    @Value("${api.task.url}")
    private String taskApiBaseUrl;

    public DataAggregationServiceImpl(@Qualifier("authenticatedRestTemplate") RestTemplate taskRestTemplate,
                                      AccountService accountService) {
        this.accountService = accountService;
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
        TaskApiResponseDto taskApiResponse;
        try{
            String url = taskApiBaseUrl + "/projects/"+ projectId;
            taskApiResponse = taskRestTemplate.getForObject(url, TaskApiResponseDto.class);
        }catch(Exception e){
            log.error("프로젝트에 대한 프로젝트 디테일을 가져오는데 실패했습니다 {}: {}", projectId,e.getMessage());
            throw new RuntimeException("프로젝트 상세 정보 조회에 실패했습니다.");
        }

        if(taskApiResponse == null){
            throw new RuntimeException("프로젝트 상세 정보 조회 결과가 없습니다.");
        }

        List<Long> memberIds = taskApiResponse.getMembers();
        List<AccountDto> memberDetails;

        try{
            memberDetails = accountService.getAccountDetailsByIds(memberIds);
        }catch (Exception e){
            log.warn("Account-API에서 멤버 정보를 가져오는데 실패했습니다: {}", e.getMessage());
            memberDetails=List.of();
        }

        ProjectDetailsDto finalDetails = new ProjectDetailsDto(taskApiResponse);
        finalDetails.setMembers(memberDetails);
        return finalDetails;
    }


    @Override
    public void createProject(ProjectCreateRequest request) {
        try{
            String url = taskApiBaseUrl + "/projects";
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ProjectCreateRequest> httpEntity = new HttpEntity<>(request, httpHeaders);

            taskRestTemplate.postForEntity(url, httpEntity, Void.class);
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
            log.error("Project API communication failed during delete: {}", ex.getMessage());
            throw new RuntimeException("프로젝트 삭제 중 서버 오류가 발생했습니다.");
        }
    }

    @Override
    public List<Long> getProjectMembersIds(Long projectId) {
        String url = taskApiBaseUrl + "/projects/"+ projectId +"/members";

        try{
            ParameterizedTypeReference<List<Long>> responseType = new ParameterizedTypeReference<List<Long>>() {};

            ResponseEntity<List<Long>> response = taskRestTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    responseType
            );

            return response.getBody() != null ? response.getBody() : List.of();
        } catch (Exception e) {
            log.error("Task-API멤버 ID 목록 조회 실패 (projectId={}): {}", projectId, e.getMessage());
            throw new RuntimeException("멤버 목록 조회 중 오류가 발생했습니다.");
        }


    }

    @Override
    public void addProjectMember(Long projectId, Long userId) {
        String url = taskApiBaseUrl+"/projects/"+ projectId + "/members";

        ProjectMemberAddRequest request =  new ProjectMemberAddRequest(userId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ProjectMemberAddRequest> entity= new HttpEntity<>(request, headers);

        try{
            taskRestTemplate.postForEntity(url, entity, Void.class);
            log.info("프로젝트 멤버 추가 성공: projectId={}, userId={}", projectId, userId);
        }catch (HttpClientErrorException ex){
            log.warn("Task-API 멤버 추가 실패 (Client Error {}): {}", ex.getStatusCode(), projectId);
            throw new IllegalArgumentException("멤버 추가 실패 (API 오류)");
        }catch (Exception e) {
            log.error("Task-API멤버 추가 중 알 수 없는 오류 (projectId={}): {}", projectId, e.getMessage());
            throw new RuntimeException("멤버 추가 중 오류가 발생했습니다.");
        }

    }

    @Override
    public void removeProjectMember(Long projectId, Long userId) {
        String url = taskApiBaseUrl + "/projects/" + projectId + "/members/"+userId;
        try {
            taskRestTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
            log.info("프로젝트 멤버 삭제 성공: projectId={}, userId={}", projectId, userId);
        }catch (HttpClientErrorException ex){
            log.warn("Task-API 멤버 삭제 실패 (Client Error {}): {}", ex.getStatusCode(), projectId);
            throw new IllegalArgumentException("멤버 삭제 실패 (API 오류)");
        }catch (Exception e) {
            log.error("Task-API멤버 삭제 중 알 수 없는 오류 (projectId={}): {}", projectId, e.getMessage());
            throw new RuntimeException("멤버 삭제 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void createTask(Long projectId, TaskCreateRequest request) {
        String url = taskApiBaseUrl+ "/projects/"+projectId+"/tasks";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TaskCreateRequest> httpEntity = new HttpEntity<>(request, httpHeaders);
        try{
            taskRestTemplate.postForEntity(url, httpEntity, Void.class);

        } catch (Exception e){
            log.error("Task 생성에 실패했습니다. {}:{}", request.getTitle(), e.getMessage());
            throw new RuntimeException("프로젝트 생성에 실패했습니다.",e);
        }
    }

    @Override
    public TaskDetailsDto getTaskDetails(Long taskId) {
        try{
            String url = taskApiBaseUrl + "/tasks/"+ taskId;

            return taskRestTemplate.getForObject(url, TaskDetailsDto.class);
        }catch(Exception e){
            log.error("Task에대한 Task 디테일을 가져오는데 실패했습니다 {}: {}", taskId,e.getMessage());
            throw new RuntimeException("Task 상세 정보 조회에 실패했습니다.");
        }
    }

    @Override
    public void updateTask(Long taskId, TaskUpdateRequest request) {
        String url = taskApiBaseUrl + "/tasks/" + taskId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TaskUpdateRequest> httpEntity = new HttpEntity<>(request, headers);

        try{
            taskRestTemplate.exchange(url, HttpMethod.PUT, httpEntity, Void.class);
        }catch (HttpClientErrorException ex){
            log.warn("Task update Failed(Client Error{}): {}", ex.getStatusCode(), taskId);
            throw new IllegalArgumentException("정보 수정에 실패했습니다.: "+ ex.getMessage());
        }catch (Exception ex){
            log.error("Task API communication failed during update: {}", ex.getMessage());
            throw new RuntimeException("정보 수정 중 서버 오류가 발생했습니다.");
        }
    }

    @Override
    public void deleteTask(Long taskId) {
        String url = taskApiBaseUrl + "/tasks/" +taskId;
        try{
            taskRestTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
            log.info("Task 삭제가 성공적으로 됐습니다 (Task-API): {}", taskId);
        }catch (HttpClientErrorException ex){
            log.warn("Task delete Failed(Client Error{}): {}", ex.getStatusCode(), taskId);
            throw new IllegalArgumentException("프로젝트 삭제에 실패했습니다.: "+ ex.getMessage());
        }catch (Exception ex){
            log.error("Task API communication failed during delete: {}", ex.getMessage());
            throw new RuntimeException("프로젝트 삭제 중 서버 오류가 발생했습니다.");
        }
    }

    @Override
    public void setMilestoneOnTask(Long taskId, TaskSetMileStoneRequest request) {
        String url = taskApiBaseUrl  + "/tasks/" + taskId + "/milestone";

        HttpEntity<TaskSetMileStoneRequest> entity = new HttpEntity<>(request);
        entity.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        try{
            taskRestTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        }catch (HttpClientErrorException ex){
            log.warn("TaskSetMilestone update Failed(Client Error{}): taskId:{}, milestoneId:{}", ex.getStatusCode(), taskId,request.getMileStonId());
            throw new IllegalArgumentException("정보 수정에 실패했습니다.: "+ ex.getMessage());
        }catch (Exception ex){
            log.error("TaskSetMilestone communication failed during update: {}", ex.getMessage());
            throw new RuntimeException("정보 수정 중 서버 오류가 발생했습니다.");
        }
    }

    @Override
    public void addTagToTask(Long taskId, TaskAddTagRequest request) {
        String url = taskApiBaseUrl + "/tasks/"+taskId+"/tags";
        HttpEntity<TaskAddTagRequest> entity = new HttpEntity<>(request);
        entity.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try{
            taskRestTemplate.postForEntity(url, entity, Void.class);
        }catch (HttpClientErrorException ex){
            log.warn("addTagToTask update Failed(Client Error{}): taskId:{}, tagId:{}", ex.getStatusCode(), taskId, request.getTagId());
            throw new IllegalArgumentException("정보 추가에 실패했습니다.: "+ ex.getMessage());
        }catch (Exception ex){
            log.error("addTagToTask communication failed during update: {}", ex.getMessage());
            throw new RuntimeException("정보 추가 중 서버 오류가 발생했습니다.");
        }
    }

    @Override
    public void removeTagFromTask(Long taskId, Long tagId) {
        String url = taskApiBaseUrl +"/tasks/"+taskId+"/tags/"+tagId;

        try{
            taskRestTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
            log.info("Task-Tag 삭제가 성공적으로 됐습니다 (Task-API): {}", taskId);
        }catch (HttpClientErrorException ex){
            log.warn("Task-Tag delete Failed(Client Error{}): taskId:{}, tagId:{}", ex.getStatusCode(), taskId, tagId);
            throw new IllegalArgumentException("프로젝트 삭제에 실패했습니다.: "+ ex.getMessage());
        }catch (Exception ex){
            log.error("Task-Tag  communication failed during delete: {}", ex.getMessage());
            throw new RuntimeException("프로젝트 삭제 중 서버 오류가 발생했습니다.");
        }
    }

    @Override
    public void createTag(Long projectId, TagCreateRequest request) {
        String url = taskApiBaseUrl+ "/projects/"+projectId+"/tags";
        request.setProjectId(projectId);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TagCreateRequest> httpEntity = new HttpEntity<>(request, httpHeaders);
        try{
            taskRestTemplate.postForEntity(url, httpEntity, Void.class);

        }catch (HttpClientErrorException ex){
            log.warn("Task-API 태그 생성 실패 (Client Error {}): {}", ex.getStatusCode(), ex.getMessage());
            throw new IllegalArgumentException("태그 생성에 실패했습니다. (API오류)");
        }
        catch (Exception e){
            log.error("Tag 생성에 실패했습니다. {}:{}", request.getName(), e.getMessage());
            throw new RuntimeException("Tag 생성에 실패했습니다. 서버오류",e);
        }
    }

    @Override
    public void updateTag(Long projectId, Long tagId, TagUpdateRequest request) {
        String url = taskApiBaseUrl+ "/projects/"+projectId+"/tags/"+tagId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TagUpdateRequest> httpEntity = new HttpEntity<>(request, headers);

        try{
            taskRestTemplate.exchange(url, HttpMethod.PUT, httpEntity, Void.class);
        }catch (HttpClientErrorException ex){
            log.warn("Tag update Failed(Client Error{}): {}", ex.getStatusCode(), tagId);
            throw new IllegalArgumentException("정보 수정에 실패했습니다.: "+ ex.getMessage());
        }catch (Exception ex){
            log.error("Tag API communication failed during update: {}", ex.getMessage());
            throw new RuntimeException("정보 수정 중 서버 오류가 발생했습니다.");
        }
    }

    @Override
    public void deleteTag(Long projectId, Long tagId) {
        String url = taskApiBaseUrl+ "/projects/"+projectId+"/tags/"+tagId;
        try{
            taskRestTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
            log.info("Tag 삭제가 성공적으로 됐습니다 (Task-API): {}", tagId);
        }catch (HttpClientErrorException ex){
            log.warn("Tag delete Failed(Client Error{}): {}", ex.getStatusCode(), tagId);
            throw new IllegalArgumentException("프로젝트 삭제에 실패했습니다.: "+ ex.getMessage());
        }catch (Exception ex){
            log.error("Tag API communication failed during delete: {}", ex.getMessage());
            throw new RuntimeException("프로젝트 삭제 중 서버 오류가 발생했습니다.");
        }
    }

    @Override
    public void createMileStone(Long projectId, MileStoneCreateRequest request) {
        String url = taskApiBaseUrl+ "/projects/"+projectId+"/milestones";
        request.setProjectId(projectId);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MileStoneCreateRequest> httpEntity = new HttpEntity<>(request, httpHeaders);
        try{
            taskRestTemplate.postForEntity(url, httpEntity, Void.class);

        }catch (HttpClientErrorException ex){
            log.warn("Task-API 마일스톤 생성 실패 (Client Error {}): {}", ex.getStatusCode(), ex.getMessage());
            throw new IllegalArgumentException("마일 스톤 생성에 실패했습니다. (API오류)");
        }
        catch (Exception e){
            log.error("마일스톤 생성에 실패했습니다. {}:{}", request.getName(), e.getMessage());
            throw new RuntimeException("마일스톤 생성에 실패했습니다. 서버오류",e);
        }
    }

    @Override
    public void updateMilestone(Long projectId, Long milestoneId, MileStoneUpdateRequest request) {
        String url = taskApiBaseUrl+ "/projects/"+projectId+"/milestones/"+milestoneId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MileStoneUpdateRequest> httpEntity = new HttpEntity<>(request, headers);

        try{
            taskRestTemplate.exchange(url, HttpMethod.PUT, httpEntity, Void.class);
        }catch (HttpClientErrorException ex){
            log.warn("Milestone update Failed(Client Error{}): {}", ex.getStatusCode(), milestoneId);
            throw new IllegalArgumentException("정보 수정에 실패했습니다.: "+ ex.getMessage());
        }catch (Exception ex){
            log.error("Milestone API communication failed during update: {}", ex.getMessage());
            throw new RuntimeException("정보 수정 중 서버 오류가 발생했습니다.");
        }
    }

    @Override
    public void deleteMilestone(Long projectId, Long milestoneId) {
        String url = taskApiBaseUrl+ "/projects/"+projectId+"/milestones/"+milestoneId;
        try{
            taskRestTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
            log.info("Milestone 삭제가 성공적으로 됐습니다 (Task-API): {}", milestoneId);
        }catch (HttpClientErrorException ex){
            log.warn("Milestone delete Failed(Client Error{}): {}", ex.getStatusCode(), milestoneId);
            throw new IllegalArgumentException("프로젝트 삭제에 실패했습니다.: "+ ex.getMessage());
        }catch (Exception ex){
            log.error("Milestone API communication failed during delete: {}", ex.getMessage());
            throw new RuntimeException("프로젝트 삭제 중 서버 오류가 발생했습니다.");
        }
    }

    @Override
    public void createComment(Long projectId, Long taskId, CommentCreateRequest request) {
        String url = taskApiBaseUrl + "/projects/" + projectId + "/tasks/"+ taskId + "/comments";
        request.setTaskId(taskId);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CommentCreateRequest> httpEntity = new HttpEntity<>(request, httpHeaders);

        try{
            taskRestTemplate.postForEntity(url, httpEntity, Void.class);
            log.info("코멘트 생성 성공: projectId={}, tasksId={}", projectId, taskId);
        }catch (HttpClientErrorException ex){
            log.warn("Task-API Comment 생성 실패 (Client Error {}): {}", ex.getStatusCode(), ex.getMessage());
            throw new IllegalArgumentException("Comment 생성에 실패했습니다. (API오류)");
        }
        catch (Exception e){
            log.error("코멘트 생성에 실패했습니다. {}:{}", request.getTaskId(), e.getMessage());
            throw new RuntimeException("코멘트 생성에 실패했습니다. 서버오류",e);
        }
    }

    @Override
    public void updateComment(Long projectId, Long taskId, Long commentId, CommentUpdateRequest request) {
        String url = taskApiBaseUrl + "/projects/" + projectId + "/tasks/"+ taskId + "/comments/"+ commentId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CommentUpdateRequest> httpEntity = new HttpEntity<>(request, headers);

        try{
            taskRestTemplate.exchange(url, HttpMethod.PUT, httpEntity, Void.class);
        }catch (HttpClientErrorException ex){
            log.warn("Comment update Failed(Client Error{}): {}", ex.getStatusCode(), commentId);
            throw new IllegalArgumentException("정보 수정에 실패했습니다.: "+ ex.getMessage());
        }catch (Exception ex){
            log.error("Comment API communication failed during update: {}", ex.getMessage());
            throw new RuntimeException("정보 수정 중 서버 오류가 발생했습니다.");
        }
    }

    @Override
    public void deleteComment(Long projectId, Long taskId, Long commentId) {
        String url = taskApiBaseUrl + "/projects/" + projectId + "/tasks/"+ taskId + "/comments/"+ commentId;
        try{
            taskRestTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
            log.info("Comment 삭제가 성공적으로 됐습니다 (Task-API): {}", commentId);
        }catch (HttpClientErrorException ex){
            log.warn("Comment delete Failed(Client Error{}): {}", ex.getStatusCode(), commentId);
            throw new IllegalArgumentException("Comment 삭제에 실패했습니다.: "+ ex.getMessage());
        }catch (Exception ex){
            log.error("Comment API communication failed during delete: {}", ex.getMessage());
            throw new RuntimeException("Comment 삭제 중 서버 오류가 발생했습니다.");
        }
    }
}
