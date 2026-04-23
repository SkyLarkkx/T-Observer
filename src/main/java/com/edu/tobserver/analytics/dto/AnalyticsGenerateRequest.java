package com.edu.tobserver.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsGenerateRequest {

    private String teacherName;
    private String startTime;
    private String endTime;
}
