package com.nhnacademy.gateway.controller;

import com.nhnacademy.gateway.config.CustomUserPrincipal;
import com.nhnacademy.gateway.dto.basic.AccountDto;
import com.nhnacademy.gateway.dto.basic.CommentDto;
import com.nhnacademy.gateway.dto.create.TaskCreateRequest;
import com.nhnacademy.gateway.dto.detail.ProjectDetailsDto;
import com.nhnacademy.gateway.dto.detail.TaskDetailsDto;
import com.nhnacademy.gateway.dto.relation.TaskAddTagRequest;
import com.nhnacademy.gateway.dto.relation.TaskSetMileStoneRequest;
import com.nhnacademy.gateway.dto.update.TaskUpdateRequest;
import com.nhnacademy.gateway.service.AccountService;
import com.nhnacademy.gateway.service.DataAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping
@Slf4j
public class TaskController {
    private final DataAggregationService aggregationService;
    private final AccountService accountService;

    public TaskController(DataAggregationService aggregationService, AccountService accountService) {
        this.aggregationService = aggregationService;
        this.accountService = accountService;
    }

    @GetMapping("/projects/{projectId}/tasks/register")
    public String createTaskForm(@PathVariable Long projectId, Model model){
        model.addAttribute("task", new TaskCreateRequest());
        model.addAttribute("projectId", projectId);
        // 마일스톤 필드 추가로 인한 로직 추가
        model.addAttribute("projectDetails", aggregationService.getProjectDetails(projectId));
        return "TaskCreateForm";
    }

    @PostMapping("/projects/{projectId}/tasks")
    public String createTask(@PathVariable Long projectId, @ModelAttribute TaskCreateRequest request, RedirectAttributes redirectAttributes){
        try{
            aggregationService.createTask(projectId,request);
            redirectAttributes.addFlashAttribute("message", "새 태스크가 생성되었습니다.");
            return "redirect:/projects/"+projectId;
        }catch(Exception e){
            log.error("Task 생성실패 (projectId={}): {}", projectId, e.getMessage() );
            redirectAttributes.addFlashAttribute("error", "태스트 생성 실패");
            return "redirect:/projects/"+projectId+"/tasks/register";
        }
    }

    @GetMapping("/projects/{projectId}/tasks/{taskId}")
    public String getTaskDetails(@PathVariable Long taskId, @PathVariable Long projectId, Model model){

        TaskDetailsDto taskDetails = aggregationService.getTaskDetails(projectId,taskId);
        ProjectDetailsDto projectDetails = aggregationService.getProjectDetails(projectId);

        Map<Long, String> commentWriterMap = new HashMap<>();

        if (taskDetails.getComments() != null && !taskDetails.getComments().isEmpty()) {
            List<Long> writerIds = taskDetails.getComments().stream()
                    .map(CommentDto::getWriterId)
                    .distinct()
                    .collect(Collectors.toList());

            List<AccountDto> writers = accountService.getAccountDetailsByIds(writerIds);

            commentWriterMap = writers.stream()
                    .collect(Collectors.toMap(AccountDto::getAccountId, AccountDto::getUsername));
        }


        model.addAttribute("commentWriterMap", commentWriterMap);

        model.addAttribute("projectId", projectId);
        model.addAttribute("taskDetails", taskDetails);
        model.addAttribute("allTags", projectDetails.getTags());
        model.addAttribute("allMilestones", projectDetails.getMileStones());
        model.addAttribute("currentAccountId", getCurrentUserAccountId());
        return "taskDetails";

    }

    @GetMapping("/projects/{projectId}/tasks/{taskId}/edit")
    public String showTaskUpdateForm(@PathVariable Long projectId, @PathVariable Long taskId, Model model){
        try{
            TaskDetailsDto taskDetails = aggregationService.getTaskDetails(projectId,taskId);

            TaskUpdateRequest request = new TaskUpdateRequest(
                    taskDetails.getTask().getTitle(),
                    taskDetails.getTask().getContent(),
                    taskDetails.getTask().getMilestoneId()
            );
            model.addAttribute("request", request);
            model.addAttribute("taskId", taskId);
            model.addAttribute("projectId", projectId);
            return "taskEditForm";
        }catch (Exception e){
            log.error("Task 수정 폼 로드 실패 (TaskId={}): {}", taskId, e.getMessage());
            return "redirect:/projects/"+ projectId;
        }
    }
    @PutMapping("/projects/{projectId}/tasks/{taskId}")
    public String updateTask(@PathVariable Long projectId, @PathVariable Long taskId, @ModelAttribute TaskUpdateRequest request, RedirectAttributes redirectAttributes){
        try{
            aggregationService.updateTask(projectId,taskId, request);
            redirectAttributes.addFlashAttribute("message", "태스크가 수정되었습니다");
            return "redirect:/projects/"+projectId+"/tasks/"+taskId;
        }catch (Exception e){
            log.error("Task 수정 실패 (taskId={}): {}", taskId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "수정에 실패했습니다."+ e.getMessage());
            return "redirect:/projects/"+projectId+"/tasks/"+taskId+"/edit";
        }
    }

    @DeleteMapping("/projects/{projectId}/tasks/{taskId}")
    public String deleteTask(@PathVariable Long projectId, @PathVariable Long taskId, RedirectAttributes redirectAttributes){
        try{
            aggregationService.deleteTask(projectId,taskId);
            redirectAttributes.addFlashAttribute("message", "태스크가 삭제되었습니다.");
        }catch (Exception e){
            log.error("Task 삭제 실패 (taskId={}): {}", taskId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "삭제에 실패했습니다."+ e.getMessage());

        }
        return "redirect:/projects/"+projectId;
    }

    @PutMapping("/projects/{projectId}/tasks/{taskId}/milestone")
    public String setMileStone(@PathVariable Long taskId, @PathVariable Long projectId, @ModelAttribute TaskSetMileStoneRequest request,
                               RedirectAttributes redirectAttributes){

        try {
            TaskDetailsDto taskDetails = aggregationService.getTaskDetails(projectId, taskId);
            TaskUpdateRequest updateRequest = new TaskUpdateRequest(
                    taskDetails.getTask().getTitle(),
                    taskDetails.getTask().getContent(),
                    request.getMileStonId()
            );
            aggregationService.updateTask(projectId, taskId, updateRequest);
            redirectAttributes.addFlashAttribute("message", "마일스톤이 설정되었습니다.");
        } catch (Exception e) {
            log.error("마일스톤 설정 실패 (taskId={}): {}", taskId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "마일스톤 설정에 실패했습니다.");
        }
        return "redirect:/projects/"+projectId+"/tasks/"+taskId;
    }

    @PostMapping("projects/{projectId}/tasks/{taskId}/tags")
    public String addTag(@PathVariable Long taskId,@PathVariable Long projectId, @ModelAttribute TaskAddTagRequest request){
        aggregationService.addTagToTask(projectId,taskId, request);
        return "redirect:/projects/"+projectId+"/tasks/"+taskId;
    }

    @DeleteMapping("projects/{projectId}/tasks/{taskId}/tags/{tagId}")
    public String deleteTag(@PathVariable Long taskId,@PathVariable Long projectId, @PathVariable Long tagId ){
        aggregationService.removeTagFromTask(projectId,taskId, tagId);
        return "redirect:/projects/"+projectId+"/tasks/"+taskId;
    }

    private Long getCurrentUserAccountId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() instanceof CustomUserPrincipal){
            return ((CustomUserPrincipal) authentication.getPrincipal()) .getNumericId();
        }
        return null;
    }
}
