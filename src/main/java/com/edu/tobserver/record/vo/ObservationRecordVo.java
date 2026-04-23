package com.edu.tobserver.record.vo;

import com.edu.tobserver.record.entity.ObservationScore;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ObservationRecordVo {

    private final Long id;
    private final Long taskId;
    private final Long observerId;
    private final String observerName;
    private final String teacherName;
    private final String strengths;
    private final String weaknesses;
    private final String suggestions;
    private final String status;
    private final String rejectReason;
    private final LocalDateTime submittedAt;
    private final LocalDateTime approvedAt;
    private final List<ObservationScore> scores;
}
