package com.edu.tobserver.analytics.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class SavedAnalyticsReportItemVo {

    private Long id;
    private String teacherName;
    private Integer sampleCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime generatedAt;
}
