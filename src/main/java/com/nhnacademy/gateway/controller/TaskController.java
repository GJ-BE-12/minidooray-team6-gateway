package com.nhnacademy.gateway.controller;

import com.nhnacademy.gateway.dto.create.TaskCreateRequest;
import com.nhnacademy.gateway.dto.detail.ProjectDetailsDto;
import com.nhnacademy.gateway.dto.detail.TaskDetailsDto;
import com.nhnacademy.gateway.dto.relation.TaskAddTagRequest;
import com.nhnacademy.gateway.dto.relation.TaskSetMileStoneRequest;
import com.nhnacademy.gateway.dto.update.TaskUpdateRequest;
import com.nhnacademy.gateway.service.DataAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping
@Slf4j
public class TaskController {
    private final DataAggregationService aggregationService;

    public TaskController(DataAggregationService aggregationService) {
        this.aggregationService = aggregationService;
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

        TaskDetailsDto taskDetails = aggregationService.getTaskDetails(taskId);
        ProjectDetailsDto projectDetails = aggregationService.getProjectDetails(projectId);
        model.addAttribute("projectId", projectId);
        model.addAttribute("taskDetails", taskDetails);
        model.addAttribute("allTags", projectDetails.getTags());
        return "taskDetails";

    }

    @GetMapping("/projects/{projectId}/tasks/{taskId}/edit")
    public String showTaskUpdateForm(@PathVariable Long projectId, @PathVariable Long taskId, Model model){
        try{
            TaskDetailsDto taskDetails = aggregationService.getTaskDetails(taskId);

            TaskUpdateRequest request = new TaskUpdateRequest(
                    taskDetails.getTask().getTitle(),
                    taskDetails.getTask().getContent()
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
            aggregationService.updateTask(taskId, request);
            redirectAttributes.addFlashAttribute("message", "태스크가 수정되었습니다");
            return "redirect:/tasks/"+taskId;
        }catch (Exception e){
            log.error("Task 수정 실패 (taskId={}): {}", taskId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "수정에 실패했습니다."+ e.getMessage());
            return "redirect:/projects/"+projectId+"/tasks/"+taskId+"/edit";
        }
    }

    @DeleteMapping("/projects/{projectId}/tasks/{taskId}")
    public String deleteTask(@PathVariable Long projectId, @PathVariable Long taskId, RedirectAttributes redirectAttributes){
        try{
            aggregationService.deleteTask(taskId);
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
        aggregationService.setMilestoneOnTask(taskId,request);
        redirectAttributes.addFlashAttribute("message", "마일스톤이 설정되었습니다.");
        return "redirect:/projects/"+projectId+"/tasks/"+taskId;
    }

    @PostMapping("projects/{projectId}/tasks/{taskId}/tags")
    public String addTag(@PathVariable Long taskId,@PathVariable Long projectId, @ModelAttribute TaskAddTagRequest request){
        aggregationService.addTagToTask(taskId, request);
        return "redirect:/projects/"+projectId+"/tasks/"+taskId;
    }

    @DeleteMapping("projects/{projectId}/tasks/{taskId}/tags/{tagId}")
    public String deleteTag(@PathVariable Long taskId,@PathVariable Long projectId, @PathVariable Long tagId ){
        aggregationService.removeTagFromTask(taskId, tagId);
        return "redirect:/projects/"+projectId+"/tasks/"+taskId;
    }
}
