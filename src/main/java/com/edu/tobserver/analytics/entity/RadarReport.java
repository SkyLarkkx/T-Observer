package com.edu.tobserver.analytics.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RadarReport {

    private Long id;
    private Long leaderId;
    private String teacherName;
    private String periodType;
    private String periodValue;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer sampleCount;
    private String radarJson;
    private String strengthSummary;
    private String weaknessSummary;
    private String conclusion;
    private LocalDateTime generatedAt;
}
