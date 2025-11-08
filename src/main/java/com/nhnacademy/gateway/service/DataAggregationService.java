package com.nhnacademy.gateway.service;

import com.nhnacademy.gateway.dto.create.ProjectCreateRequest;
import com.nhnacademy.gateway.dto.detail.ProjectDetailsDto;
import com.nhnacademy.gateway.dto.basic.ProjectDto;
import com.nhnacademy.gateway.dto.update.ProjectUpdateRequest;

import java.util.Map;

public interface DataAggregationService {
    Map<String, Object> getDashboardData(String userId);

    ProjectDetailsDto getProjectDetails(Long projectId);

    ProjectCreateRequest createProject(ProjectCreateRequest request);

    ProjectDto getProject(Long projectId);

    void updateProject(Long projectId, ProjectUpdateRequest request);
}
