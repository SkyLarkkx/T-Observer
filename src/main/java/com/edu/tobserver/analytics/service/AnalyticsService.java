package com.edu.tobserver.analytics.service;

import com.edu.tobserver.analytics.dto.AnalyticsGenerateRequest;
import com.edu.tobserver.analytics.entity.RadarReport;
import com.edu.tobserver.analytics.mapper.AnalyticsMapper;
import com.edu.tobserver.analytics.vo.AnalyticsReportVo;
import com.edu.tobserver.common.context.LoginUser;
import com.edu.tobserver.common.context.LoginUserContext;
import com.edu.tobserver.common.enums.DimensionCode;
import com.edu.tobserver.common.enums.RoleCode;
import com.edu.tobserver.common.exception.BusinessException;
import com.edu.tobserver.record.entity.ObservationRecord;
import com.edu.tobserver.record.entity.ObservationScore;
import com.edu.tobserver.record.mapper.ObservationScoreMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private static final String LOW_SAMPLE_CONCLUSION = "样本不足，暂不生成雷达图";

    private final AnalyticsMapper analyticsMapper;
    private final ObservationScoreMapper observationScoreMapper;

    public AnalyticsService(AnalyticsMapper analyticsMapper,
                            ObservationScoreMapper observationScoreMapper) {
        this.analyticsMapper = analyticsMapper;
        this.observationScoreMapper = observationScoreMapper;
    }

    public AnalyticsReportVo generate(AnalyticsGenerateRequest request) {
        requireLeader();
        TimeRange timeRange = parseTimeRange(request.getPeriodType(), request.getPeriodValue());
        List<ObservationRecord> approvedRecords = analyticsMapper.findApprovedRecords(
                request.getTeacherName().trim(),
                timeRange.startTime(),
                timeRange.endTime());

        String strengthSummary = joinTexts(approvedRecords, ObservationRecord::getStrengths);
        String weaknessSummary = joinTexts(approvedRecords, ObservationRecord::getWeaknesses);

        AnalyticsReportVo.RadarChartVo radarChart = null;
        String conclusion;
        if (approvedRecords.size() < 3) {
            conclusion = LOW_SAMPLE_CONCLUSION;
        } else {
            radarChart = buildRadarChart(approvedRecords);
            conclusion = "已根据 " + approvedRecords.size() + " 条已审批记录生成雷达图";
        }

        AnalyticsReportVo report = AnalyticsReportVo.builder()
                .teacherName(request.getTeacherName().trim())
                .periodType(request.getPeriodType().trim())
                .periodValue(request.getPeriodValue().trim())
                .sampleCount(approvedRecords.size())
                .radarChart(radarChart)
                .strengthSummary(strengthSummary)
                .weaknessSummary(weaknessSummary)
                .conclusion(conclusion)
                .build();

        persistReport(report);
        return report;
    }

    private void requireLeader() {
        LoginUser loginUser = LoginUserContext.getRequired();
        if (loginUser.getRoleCode() != RoleCode.LEADER) {
            throw new BusinessException(403, "只有组长可以生成教师分析");
        }
    }

    private TimeRange parseTimeRange(String periodType, String periodValue) {
        String normalizedType = periodType.trim().toUpperCase();
        String normalizedValue = periodValue.trim();
        return switch (normalizedType) {
            case "MONTH" -> {
                YearMonth yearMonth = YearMonth.parse(normalizedValue);
                yield new TimeRange(yearMonth.atDay(1).atStartOfDay(), yearMonth.plusMonths(1).atDay(1).atStartOfDay());
            }
            case "YEAR" -> {
                Year year = Year.parse(normalizedValue);
                yield new TimeRange(year.atDay(1).atStartOfDay(), year.plusYears(1).atDay(1).atStartOfDay());
            }
            default -> throw new BusinessException(400, "统计周期类型仅支持 MONTH 或 YEAR");
        };
    }

    private AnalyticsReportVo.RadarChartVo buildRadarChart(List<ObservationRecord> approvedRecords) {
        Map<DimensionCode, BigDecimal> totalScores = new EnumMap<>(DimensionCode.class);
        Map<DimensionCode, Integer> scoreCounts = new EnumMap<>(DimensionCode.class);
        Map<DimensionCode, String> dimensionNames = new EnumMap<>(DimensionCode.class);

        for (ObservationRecord approvedRecord : approvedRecords) {
            List<ObservationScore> scores = observationScoreMapper.findByRecordId(approvedRecord.getId());
            for (ObservationScore score : scores) {
                DimensionCode dimensionCode = DimensionCode.valueOf(score.getDimensionCode());
                totalScores.merge(dimensionCode, score.getScoreValue(), BigDecimal::add);
                scoreCounts.merge(dimensionCode, 1, Integer::sum);
                dimensionNames.putIfAbsent(dimensionCode, score.getDimensionName());
            }
        }

        List<AnalyticsReportVo.IndicatorVo> indicators = new ArrayList<>();
        List<BigDecimal> values = new ArrayList<>();
        for (DimensionCode dimensionCode : DimensionCode.values()) {
            indicators.add(AnalyticsReportVo.IndicatorVo.builder()
                    .name(resolveDimensionName(dimensionCode, dimensionNames.get(dimensionCode)))
                    .max(5)
                    .build());
            BigDecimal total = totalScores.getOrDefault(dimensionCode, BigDecimal.ZERO);
            int count = scoreCounts.getOrDefault(dimensionCode, 0);
            BigDecimal average = count == 0
                    ? BigDecimal.ZERO
                    : total.divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);
            values.add(average);
        }

        return AnalyticsReportVo.RadarChartVo.builder()
                .indicators(indicators)
                .values(values)
                .build();
    }

    private String resolveDimensionName(DimensionCode dimensionCode, String configuredName) {
        if (configuredName != null && !configuredName.isBlank()) {
            return configuredName;
        }
        return switch (dimensionCode) {
            case TEACHING_DESIGN -> "Teaching Design";
            case CLASSROOM_ORGANIZATION -> "Classroom Organization";
            case TEACHING_CONTENT -> "Teaching Content";
            case INTERACTION_FEEDBACK -> "Interaction Feedback";
            case TEACHING_EFFECTIVENESS -> "Teaching Effectiveness";
        };
    }

    private String joinTexts(List<ObservationRecord> approvedRecords, Function<ObservationRecord, String> extractor) {
        List<String> parts = new ArrayList<>();
        for (ObservationRecord approvedRecord : approvedRecords) {
            String value = extractor.apply(approvedRecord);
            if (value != null && !value.isBlank()) {
                parts.add(value.trim());
            }
        }
        return String.join("；", parts);
    }

    private void persistReport(AnalyticsReportVo report) {
        RadarReport radarReport = new RadarReport();
        radarReport.setTeacherName(report.getTeacherName());
        radarReport.setPeriodType(report.getPeriodType());
        radarReport.setPeriodValue(report.getPeriodValue());
        radarReport.setSampleCount(report.getSampleCount());
        radarReport.setRadarJson(toRadarJson(report.getRadarChart()));
        radarReport.setStrengthSummary(report.getStrengthSummary());
        radarReport.setWeaknessSummary(report.getWeaknessSummary());
        radarReport.setConclusion(report.getConclusion());
        analyticsMapper.insert(radarReport);
    }

    private String toRadarJson(AnalyticsReportVo.RadarChartVo radarChart) {
        if (radarChart == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("{\"indicators\":[");
        for (int i = 0; i < radarChart.getIndicators().size(); i++) {
            AnalyticsReportVo.IndicatorVo indicator = radarChart.getIndicators().get(i);
            if (i > 0) {
                builder.append(',');
            }
            builder.append("{\"name\":\"")
                    .append(escapeJson(indicator.getName()))
                    .append("\",\"max\":")
                    .append(indicator.getMax())
                    .append('}');
        }
        builder.append("],\"values\":[");
        for (int i = 0; i < radarChart.getValues().size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(radarChart.getValues().get(i));
        }
        builder.append("]}");
        return builder.toString();
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record TimeRange(LocalDateTime startTime, LocalDateTime endTime) {
    }
}
