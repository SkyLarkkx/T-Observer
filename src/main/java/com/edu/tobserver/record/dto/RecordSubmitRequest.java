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
public class RecordSubmitRequest {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotBlank(message = "授课教师不能为空")
    private String teacherName;

    @NotBlank(message = "优点不能为空")
    private String strengths;

    @NotBlank(message = "待改进项不能为空")
    private String weaknesses;

    @NotBlank(message = "改进建议不能为空")
    private String suggestions;

    @NotNull(message = "评分不能为空")
    private List<ScoreInput> scores;
}
