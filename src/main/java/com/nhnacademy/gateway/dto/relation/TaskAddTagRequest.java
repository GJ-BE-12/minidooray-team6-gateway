package com.nhnacademy.gateway.dto.relation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Task에 tag 추가
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskAddTagRequest {
    private Long tagId;
}
