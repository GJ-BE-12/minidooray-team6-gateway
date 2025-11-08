package com.nhnacademy.gateway.dto.basic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MileStone 정보를 담음/ 주로 Project DetailsDto내의 MileStone 목록에 사용
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MileStoneDto {
    private Long id;
    private String name;

}
