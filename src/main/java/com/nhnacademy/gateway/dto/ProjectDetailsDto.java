package com.nhnacademy.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDetailsDto {
    private ProjectDto project;

    private List<TaskDto> tasks;
    private List<TagDto> tags;
    private List<MileStoneDto> mileStones;
    private List<AccountDto> members;
}
