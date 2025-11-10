package com.nhnacademy.gateway.service;


import com.nhnacademy.gateway.dto.create.ProjectCreateRequest;
import com.nhnacademy.gateway.dto.create.TaskCreateRequest;
import com.nhnacademy.gateway.dto.detail.ProjectDetailsDto;
import com.nhnacademy.gateway.dto.basic.ProjectDto;
import com.nhnacademy.gateway.dto.detail.TaskDetailsDto;
import com.nhnacademy.gateway.dto.relation.TaskAddTagRequest;
import com.nhnacademy.gateway.dto.relation.TaskSetMileStoneRequest;
import com.nhnacademy.gateway.dto.update.ProjectUpdateRequest;
import com.nhnacademy.gateway.dto.update.TaskUpdateRequest;

import java.util.List;
import java.util.Map;

public interface DataAggregationService {
    Map<String, Object> getDashboardData(String userId);

    ProjectDetailsDto getProjectDetails(Long projectId);

    ProjectCreateRequest createProject(ProjectCreateRequest request);

    ProjectDto getProject(Long projectId);

    void updateProject(Long projectId, ProjectUpdateRequest request);

    void deleteProject(Long projectId);

    List<Long> getProjectMembersIds(Long projectId);

    void addProjectMember(Long projectId, Long userId);

    void removeProjectMember(Long projectId, Long userId);

    TaskCreateRequest createTask(Long projectId, TaskCreateRequest request);

    TaskDetailsDto getTaskDetails(Long taskId);

    void updateTask(Long taskId, TaskUpdateRequest request);

    void deleteTask(Long taskId);

    void setMilestoneOnTask(Long taskId, TaskSetMileStoneRequest request);

    void addTagToTask(Long taskId, TaskAddTagRequest request);

    void removeTagFromTask(Long taskId, Long tagId);
}
