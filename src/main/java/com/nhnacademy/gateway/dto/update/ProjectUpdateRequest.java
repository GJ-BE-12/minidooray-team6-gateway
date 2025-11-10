package com.nhnacademy.gateway.dto.update;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Project 내 다른 필드를 건드리지않게 update용 request
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUpdateRequest {
    private String name;
    private String status;
}
