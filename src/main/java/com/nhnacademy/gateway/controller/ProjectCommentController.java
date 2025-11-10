package com.nhnacademy.gateway.controller;


import com.nhnacademy.gateway.dto.create.CommentCreateRequest;
import com.nhnacademy.gateway.dto.create.MileStoneCreateRequest;
import com.nhnacademy.gateway.dto.update.CommentUpdateRequest;
import com.nhnacademy.gateway.dto.update.MileStoneUpdateRequest;
import com.nhnacademy.gateway.service.DataAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/tasks/{taskId}/comments")
public class ProjectCommentController {

    private final DataAggregationService aggregationService;

    @PostMapping
    public String createComment(@PathVariable Long projectId, @PathVariable Long taskId ,@ModelAttribute CommentCreateRequest request, RedirectAttributes redirectAttributes){
        try{
            aggregationService.createComment(projectId,taskId ,request);
        }catch (Exception ex){
            log.warn("코멘트 생성 실패: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("error", "코멘트 생성에 실패했습니다.");
        }
        return "redirect:/projects/"+projectId +"/tasks/"+taskId;
    }

    @PutMapping("/{commentId}")
    public String updateComment(@PathVariable Long projectId, @PathVariable Long taskId , @PathVariable Long commentId, @ModelAttribute CommentUpdateRequest request, RedirectAttributes redirectAttributes){
        try{
            aggregationService.updateComment(projectId,taskId,commentId, request);
        }catch (Exception e){
            log.warn("코멘트 수정 실패 (commentId: {}): {}", commentId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "코멘트 수정 실패");
        }
        return "redirect:/projects/"+projectId +"/tasks/"+taskId;
    }

    @DeleteMapping("/{commentId}")
    public String deleteComment(@PathVariable Long projectId,  @PathVariable Long taskId , @PathVariable Long commentId, RedirectAttributes redirectAttributes){
        try{
            aggregationService.deleteComment(projectId, taskId, commentId);
        }catch (Exception e){
            log.warn("코멘트 삭제 실패 commentId={}: {}",commentId , e.getMessage());
            redirectAttributes.addFlashAttribute("error", "코멘트 삭제 실패");
        }
        return "redirect:/projects/"+projectId +"/tasks/"+taskId;
    }
}
