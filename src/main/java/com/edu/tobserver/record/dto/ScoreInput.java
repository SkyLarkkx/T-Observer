package com.edu.tobserver.record.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoreInput {

    private String dimensionCode;
    private String dimensionName;
    private BigDecimal scoreValue;
}
