package com.edu.tobserver.record.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordDraftRequest {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotBlank(message = "授课教师不能为空")
    private String teacherName;

    private String strengths;
    private String weaknesses;
    private String suggestions;
    private List<ScoreInput> scores;
}
