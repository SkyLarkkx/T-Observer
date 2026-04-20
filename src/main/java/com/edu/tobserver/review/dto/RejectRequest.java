package com.edu.tobserver.review.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectRequest {

    @NotBlank(message = "退回时必须填写原因")
    private String reason;
}
