package com.nhnacademy.gateway.service;

import com.nhnacademy.gateway.dto.ProjectCreateRequest;
import com.nhnacademy.gateway.dto.ProjectDetailsDto;
import com.nhnacademy.gateway.dto.ProjectDto;

import java.util.Map;

public interface DataAggregationService {
    Map<String, Object> getDashboardData(String userId);

    ProjectDetailsDto getProjectDetails(Long projectId);

    ProjectDto createProject(ProjectCreateRequest request, String adminUserId);
}
