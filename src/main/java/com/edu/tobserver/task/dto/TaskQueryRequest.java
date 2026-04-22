package com.edu.tobserver.task.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskQueryRequest {

    private Long observerId;
    private String status;
    private Integer pageNum;
    private Integer pageSize;
}
