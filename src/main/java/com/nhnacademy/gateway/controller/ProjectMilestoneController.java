package com.nhnacademy.gateway.controller;

import com.nhnacademy.gateway.dto.create.MileStoneCreateRequest;
import com.nhnacademy.gateway.dto.create.TagCreateRequest;
import com.nhnacademy.gateway.dto.update.MileStoneUpdateRequest;
import com.nhnacademy.gateway.dto.update.TagUpdateRequest;
import com.nhnacademy.gateway.service.DataAggregationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/projects/{projectId}/milestones")
public class ProjectMilestoneController {
    private final DataAggregationService aggregationService;

    public ProjectMilestoneController(DataAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @PostMapping
    public String createMilestone(@PathVariable Long projectId, @ModelAttribute MileStoneCreateRequest request, RedirectAttributes redirectAttributes){
        try{
            aggregationService.createMileStone(projectId, request);
            redirectAttributes.addFlashAttribute("message", "새 마일스톤이 생성되었습니다.");
        }catch (Exception ex){
            log.warn("마일스톤 생성 실패: {}", ex.getMessage());
            redirectAttributes.addFlashAttribute("error", "마일스톤 생성에 실패했습니다.");
        }
        return "redirect:/projects/"+projectId;
    }

    @PutMapping("/{milestoneId}")
    public String UpdateTag(@PathVariable Long projectId, @PathVariable Long milestoneId, @ModelAttribute MileStoneUpdateRequest request, RedirectAttributes redirectAttributes){
        try{
            aggregationService.updateMilestone(projectId,milestoneId, request);
        }catch (Exception e){
            log.warn("마일스톤 수정 실패 (milestoneId: {}): {}", milestoneId, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Milestone 수정 실패");
        }
        return "redirect:/projects/"+projectId;
    }
    @DeleteMapping("/{milestoneId}")
    public String DeleteTag(@PathVariable Long projectId, @PathVariable Long milestoneId, RedirectAttributes redirectAttributes){
        try{
            aggregationService.deleteMilestone(projectId, milestoneId);
        }catch (Exception e){
            log.warn("Milestone 삭제 실패 milestoneId={}: {}",milestoneId , e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Milestone 삭제 실패");
        }
        return "redirect:/projects/"+projectId;
    }
}