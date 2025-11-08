package com.nhnacademy.gateway.service;

import com.nhnacademy.gateway.dto.create.ProjectCreateRequest;
import com.nhnacademy.gateway.dto.detail.ProjectDetailsDto;
import com.nhnacademy.gateway.dto.basic.ProjectDto;

import java.util.Map;

public interface DataAggregationService {
    Map<String, Object> getDashboardData(String userId);

    ProjectDetailsDto getProjectDetails(Long projectId);

    ProjectCreateRequest createProject(ProjectCreateRequest request);
}
