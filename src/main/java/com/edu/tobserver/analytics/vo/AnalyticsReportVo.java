package com.edu.tobserver.analytics.vo;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AnalyticsReportVo {

    private final String teacherName;
    private final String periodType;
    private final String periodValue;
    private final int sampleCount;
    private final RadarChartVo radarChart;
    private final String strengthSummary;
    private final String weaknessSummary;
    private final String conclusion;

    @Getter
    @Builder
    public static class RadarChartVo {

        private final List<IndicatorVo> indicators;
        private final List<BigDecimal> values;
    }

    @Getter
    @Builder
    public static class IndicatorVo {

        private final String name;
        private final Integer max;
    }
}
