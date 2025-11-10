package com.nhnacademy.gateway.dto.detail;

import com.nhnacademy.gateway.dto.basic.CommentDto;
import com.nhnacademy.gateway.dto.basic.MileStoneDto;
import com.nhnacademy.gateway.dto.basic.TagDto;
import com.nhnacademy.gateway.dto.basic.TaskDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Task 상세조회 시 보여줄 tag 목록과 mileStones목록 comment목록
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetailsDto {

    private TaskDto task;
    private MileStoneDto mileStone;

    private List<TagDto> tags;
    private List<CommentDto>  comments;
}
