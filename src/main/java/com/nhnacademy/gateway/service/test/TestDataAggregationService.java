//package com.nhnacademy.gateway.service.test;
//
//import com.nhnacademy.gateway.dto.basic.*;
//import com.nhnacademy.gateway.dto.create.ProjectCreateRequest;
//import com.nhnacademy.gateway.dto.detail.ProjectDetailsDto;
//import com.nhnacademy.gateway.service.DataAggregationService;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicLong;
//import java.util.stream.Collectors;
//
///**
// * "test" 프로필 전용 가짜(Mock) 서비스.
// * 실제 API 대신 In-Memory 데이터베이스 역할을 수행합니다.
// */
//@Service
//@Profile("test")
//public class TestDataAggregationService implements DataAggregationService {
//
//    // 가짜 유저 정보
//    private final AccountDto testUserAccount = new AccountDto(1L, "testuser", "testuser@example.com", "ACTIVE", LocalDateTime.now(), LocalDateTime.now().plusDays(3));
//
//    // In-memory DB 역할: 프로젝트 ID를 키로, 프로젝트 상세 정보를 값으로 가짐
//    private final Map<Long, ProjectDetailsDto> fakeProjectDatabase = new ConcurrentHashMap<>();
//    private final AtomicLong projectCounter = new AtomicLong(100); // 프로젝트 ID 생성기
//    private final AtomicLong taskCounter = new AtomicLong(1000); // 태스크 ID 생성기
//
//    public TestDataAggregationService() {
//        // "test" 프로필이 시작될 때 기본 Mock 데이터를 생성
//        ProjectCreateRequest req1 = new ProjectCreateRequest();
//        req1.setName("가짜 프로젝트 1 (테스트)");
//        createProject(req1, "testuser"); // 프로젝트 1 생성 (ID: 100)
//
//        ProjectCreateRequest req2 = new ProjectCreateRequest();
//        req2.setName("가짜 프로젝트 2 (테스트)");
//        req2.setStatus("종료");
//        createProject(req2, "testuser"); // 프로젝트 2 생성 (ID: 101)
//    }
//
//    @Override
//    public Map<String, Object> getDashboardData(String userId) {
//        Map<String, Object> data = new ConcurrentHashMap<>();
//
//        // 1. 가짜 계정 정보 반환
//        data.put("account", testUserAccount);
//
//        // 2. 가짜 프로젝트 목록 In-memory DB에서 조회
//        // 요구사항: "자신이 속한 Project 목록만" -> "testuser"가 관리자인 프로젝트만 필터링
//        List<ProjectDto> userProjects = fakeProjectDatabase.values().stream()
//                .map(ProjectDetailsDto::getProject)
//                .filter(project -> project.getAdminUserId().equals(userId))
//                .collect(Collectors.toList());
//
//        data.put("projects", userProjects);
//        return data;
//    }
//
//    @Override
//    public ProjectDetailsDto getProjectDetails(Long projectId) {
//        // In-memory DB에서 프로젝트 ID로 상세 정보 조회
//        ProjectDetailsDto details = fakeProjectDatabase.get(projectId);
//        if (details == null) {
//            throw new RuntimeException("Test: Project not found with id " + projectId); // 404에 해당
//        }
//        return details;
//    }
//
//    @Override
//    public ProjectDto createProject(ProjectCreateRequest request, String adminUserId) {
//        // 새 프로젝트 DTO 생성
//        long newProjectId = projectCounter.getAndIncrement();
//        ProjectDto newProject = new ProjectDto(
//                newProjectId,
//                request.getName(),
//                request.getStatus(),
//                adminUserId
//        );
//
//        // 가짜 태스크, 태그, 마일스톤 생성
////        TaskDto fakeTask = new TaskDto(taskCounter.getAndIncrement(), "기본 태스크", "프로젝트 생성 시 자동 생성됨");
//        TagDto fakeTag = new TagDto(1L, "신규");
//        MileStoneDto fakeMilestone = new MileStoneDto(1L, "v1.0 릴리즈");
//        TaskDto taskDto = new TaskDto()
//        // 새 프로젝트의 상세 정보 객체 생성
//        ProjectDetailsDto newProjectDetails = new ProjectDetailsDto(
//                newProject,
//                List.of(fakeTask),           // 가짜 태스크 목록
//                List.of(fakeTag),            // 가짜 태그 목록
//                List.of(fakeMilestone),      // 가짜 마일스톤 목록
//                List.of(testUserAccount)     // 멤버 목록 (관리자 자신)
//        );
//
//        // In-memory DB에 저장
//        fakeProjectDatabase.put(newProjectId, newProjectDetails);
//
//        return newProject;
//    }
//}