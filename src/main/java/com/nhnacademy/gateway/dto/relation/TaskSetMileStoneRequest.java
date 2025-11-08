package com.nhnacademy.gateway.dto.relation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * task에 mileStone 설정(또는 변경)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskSetMileStoneRequest {
    private Long mileStonId;
}
