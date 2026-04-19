package com.edu.tobserver.analytics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsGenerateRequest {

    @NotBlank(message = "授课教师不能为空")
    private String teacherName;

    @NotBlank(message = "统计周期类型不能为空")
    private String periodType;

    @NotBlank(message = "统计周期值不能为空")
    private String periodValue;
}
