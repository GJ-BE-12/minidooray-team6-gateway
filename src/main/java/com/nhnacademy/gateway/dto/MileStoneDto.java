package com.nhnacademy.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MileStoneDto {
    private Long id;
    private String title;
    private LocalDate createdAt;
    private LocalDate dueDate;
}
