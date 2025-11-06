package com.nhnacademy.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskProjectDto {
    private Long projectId;
    private String name;
    private String status;
    private int taskCount;
}
