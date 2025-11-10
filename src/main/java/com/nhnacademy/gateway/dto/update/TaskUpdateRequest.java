package com.nhnacademy.gateway.dto.update;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Task 내 다른 필드를 건드리지 않는 update request
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskUpdateRequest {
    private String title;
    private String content;
    private Long milestoneId;
}

