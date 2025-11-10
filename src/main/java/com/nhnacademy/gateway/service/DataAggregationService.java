package com.nhnacademy.gateway.service;


import com.nhnacademy.gateway.dto.basic.AccountDto;
import com.nhnacademy.gateway.dto.create.*;
import com.nhnacademy.gateway.dto.detail.ProjectDetailsDto;
import com.nhnacademy.gateway.dto.basic.ProjectDto;
import com.nhnacademy.gateway.dto.detail.TaskApiResponseDto;
import com.nhnacademy.gateway.dto.detail.TaskDetailsDto;
import com.nhnacademy.gateway.dto.relation.TaskAddTagRequest;
import com.nhnacademy.gateway.dto.relation.TaskSetMileStoneRequest;
import com.nhnacademy.gateway.dto.update.*;

import java.util.List;
import java.util.Map;

public interface DataAggregationService {
    Map<String, Object> getDashboardData(String userId);

    ProjectDetailsDto getProjectDetails(Long projectId);

    void createProject(ProjectCreateRequest request);

    TaskApiResponseDto getProject(Long projectId);

    void updateProject(Long projectId, ProjectUpdateRequest request);

    void deleteProject(Long projectId);

    List<Long> getProjectMembersIds(Long projectId);

    void addProjectMember(Long projectId, Long userId);

    void removeProjectMember(Long projectId, Long userId);

    void createTask(Long projectId, TaskCreateRequest request);

    TaskDetailsDto getTaskDetails(Long projectId , Long taskId);

    void updateTask(Long projectId,Long taskId, TaskUpdateRequest request);

    void deleteTask(Long projectId,Long taskId);

//    void setMilestoneOnTask(Long projectId ,Long taskId, TaskSetMileStoneRequest request);

    void addTagToTask(Long projectId ,Long taskId, TaskAddTagRequest request);

    void removeTagFromTask(Long projectId,Long taskId, Long tagId);

    void createTag(Long projectId, TagCreateRequest request);

    void updateTag(Long projectId,Long tagId ,TagUpdateRequest request);

    void deleteTag(Long projectId, Long tagId);

    void createMileStone(Long projectId, MileStoneCreateRequest request);

    void updateMilestone(Long projectId,Long milestoneId , MileStoneUpdateRequest request);

    void deleteMilestone(Long projectId, Long milestoneId);

    void createComment(Long projectId, Long taskId, CommentCreateRequest request);

    void updateComment(Long projectId, Long taskId, Long commentId , CommentUpdateRequest request);

    void deleteComment(Long projectId, Long taskId, Long commentId );



}
