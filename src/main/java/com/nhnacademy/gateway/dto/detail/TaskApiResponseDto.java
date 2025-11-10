package com.nhnacademy.gateway.dto.detail;

import com.nhnacademy.gateway.dto.basic.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskApiResponseDto {
    private ProjectDto project;
    private List<MileStoneDto> mileStones;
    private List<TaskDto> tasks;
    private List<TagDto> tags;
    private List<Long> members;
}
