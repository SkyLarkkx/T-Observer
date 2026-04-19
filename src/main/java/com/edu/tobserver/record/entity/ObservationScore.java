package com.edu.tobserver.record.entity;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ObservationScore {

    private Long id;
    private Long recordId;
    private String dimensionCode;
    private String dimensionName;
    private BigDecimal scoreValue;
}
