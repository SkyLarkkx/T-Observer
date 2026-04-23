package com.edu.tobserver.analytics.service;

import com.edu.tobserver.analytics.dto.AnalyticsGenerateRequest;
import com.edu.tobserver.analytics.entity.RadarReport;
import com.edu.tobserver.analytics.mapper.AnalyticsMapper;
import com.edu.tobserver.analytics.vo.AnalyticsReportVo;
import com.edu.tobserver.analytics.vo.AnalyticsTeacherOptionVo;
import com.edu.tobserver.analytics.vo.SavedAnalyticsReportItemVo;
import com.edu.tobserver.common.api.PageResult;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private static final String NO_APPROVED_RECORDS_CONCLUSION =
            "\u6240\u9009\u8303\u56f4\u5185\u6682\u65e0\u5df2\u901a\u8fc7\u8bb0\u5f55\uff0c\u65e0\u6cd5\u751f\u6210\u5206\u6790";
    private static final String GENERATED_CONCLUSION_TEMPLATE =
            "\u5df2\u6839\u636e %d \u6761\u5df2\u901a\u8fc7\u8bb0\u5f55\u751f\u6210\u5206\u6790";
    private static final String ALL_APPROVED_RECORDS = "\u5168\u90e8\u5df2\u901a\u8fc7\u8bb0\u5f55";
    private static final String AFTER_SUFFIX = " \u4e4b\u540e";
    private static final String BEFORE_SUFFIX = " \u4e4b\u524d";
    private static final String TEXT_SEPARATOR = "\uff1b";
    private static final DateTimeFormatter PERIOD_VALUE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final String INDICATORS_KEY = "\"indicators\":";
    private static final String VALUES_KEY = "\"values\":";

    private final AnalyticsMapper analyticsMapper;
    private final ObservationScoreMapper observationScoreMapper;

    public AnalyticsService(AnalyticsMapper analyticsMapper,
                            ObservationScoreMapper observationScoreMapper) {
        this.analyticsMapper = analyticsMapper;
        this.observationScoreMapper = observationScoreMapper;
    }

    public List<AnalyticsTeacherOptionVo> findTeacherOptions(String keyword) {
        LoginUser loginUser = requireLeader();
        List<String> teacherNames = analyticsMapper.findTeacherNames(loginUser.getUserId(), normalizeKeyword(keyword));
        List<AnalyticsTeacherOptionVo> result = new ArrayList<>();
        for (String teacherName : teacherNames) {
            result.add(new AnalyticsTeacherOptionVo(teacherName));
        }
        return result;
    }

    public AnalyticsReportVo generate(AnalyticsGenerateRequest request) {
        LoginUser loginUser = requireLeader();
        return buildReport(loginUser.getUserId(), request);
    }

    public AnalyticsReportVo save(AnalyticsGenerateRequest request) {
        LoginUser loginUser = requireLeader();
        AnalyticsReportVo report = buildReport(loginUser.getUserId(), request);
        return persistReport(loginUser.getUserId(), report);
    }

    public PageResult<SavedAnalyticsReportItemVo> findSavedReports(Integer pageNum, Integer pageSize) {
        LoginUser loginUser = requireLeader();
        int normalizedPageNum = pageNum == null || pageNum < 1 ? DEFAULT_PAGE_NUM : pageNum;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : pageSize;
        long total = analyticsMapper.countSavedReports(loginUser.getUserId());
        List<SavedAnalyticsReportItemVo> list = analyticsMapper.findSavedReports(
                loginUser.getUserId(),
                normalizedPageSize,
                (normalizedPageNum - 1) * normalizedPageSize);
        return new PageResult<>(list, total, normalizedPageNum, normalizedPageSize);
    }

    public AnalyticsReportVo findSavedReportDetail(Long reportId) {
        LoginUser loginUser = requireLeader();
        RadarReport radarReport = analyticsMapper.findByIdAndLeaderId(reportId, loginUser.getUserId());
        if (radarReport == null) {
            throw new BusinessException(404, "\u4fdd\u5b58\u62a5\u544a\u4e0d\u5b58\u5728");
        }
        return toVo(radarReport);
    }

    private LoginUser requireLeader() {
        LoginUser loginUser = LoginUserContext.getRequired();
        if (loginUser.getRoleCode() != RoleCode.LEADER) {
            throw new BusinessException(403, "\u53ea\u6709\u7ec4\u957f\u53ef\u4ee5\u751f\u6210\u6559\u5e08\u5206\u6790");
        }
        return loginUser;
    }

    private AnalyticsReportVo buildReport(Long leaderId, AnalyticsGenerateRequest request) {
        String teacherName = normalizeTeacherName(request.getTeacherName());
        TimeRange timeRange = parseTimeRange(request.getStartTime(), request.getEndTime());
        List<ObservationRecord> approvedRecords = analyticsMapper.findApprovedRecords(
                leaderId,
                teacherName,
                timeRange.getStartTime(),
                timeRange.getEndTime());

        String strengthSummary = joinTexts(approvedRecords, ObservationRecord::getStrengths);
        String weaknessSummary = joinTexts(approvedRecords, ObservationRecord::getWeaknesses);

        AnalyticsReportVo.RadarChartVo radarChart = null;
        String conclusion;
        if (approvedRecords.isEmpty()) {
            conclusion = NO_APPROVED_RECORDS_CONCLUSION;
        } else {
            radarChart = buildRadarChart(approvedRecords);
            conclusion = GENERATED_CONCLUSION_TEMPLATE.formatted(approvedRecords.size());
        }

        return AnalyticsReportVo.builder()
                .teacherName(teacherName)
                .periodType("RANGE")
                .periodValue(formatPeriodValue(timeRange))
                .startTime(timeRange.getStartTime())
                .endTime(timeRange.getEndTime())
                .sampleCount(approvedRecords.size())
                .radarChart(radarChart)
                .strengthSummary(strengthSummary)
                .weaknessSummary(weaknessSummary)
                .conclusion(conclusion)
                .build();
    }

    private String normalizeTeacherName(String teacherName) {
        if (teacherName == null || teacherName.isBlank()) {
            throw new BusinessException(400, "\u6388\u8bfe\u6559\u5e08\u4e0d\u80fd\u4e3a\u7a7a");
        }
        return teacherName.trim();
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? null : keyword.trim();
    }

    private TimeRange parseTimeRange(String startTimeValue, String endTimeValue) {
        LocalDateTime startTime = parseOptionalTime(
                startTimeValue,
                "\u5206\u6790\u5f00\u59cb\u65f6\u95f4\u683c\u5f0f\u4e0d\u6b63\u786e");
        LocalDateTime endTime = parseOptionalTime(
                endTimeValue,
                "\u5206\u6790\u7ed3\u675f\u65f6\u95f4\u683c\u5f0f\u4e0d\u6b63\u786e");
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException(400, "\u5206\u6790\u5f00\u59cb\u65f6\u95f4\u4e0d\u80fd\u665a\u4e8e\u7ed3\u675f\u65f6\u95f4");
        }
        return new TimeRange(startTime, endTime);
    }

    private LocalDateTime parseOptionalTime(String value, String message) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new BusinessException(400, message);
        }
    }

    private String formatPeriodValue(TimeRange timeRange) {
        if (timeRange.getStartTime() != null && timeRange.getEndTime() != null) {
            return timeRange.getStartTime().format(PERIOD_VALUE_FORMATTER)
                    + " ~ "
                    + timeRange.getEndTime().format(PERIOD_VALUE_FORMATTER);
        }
        if (timeRange.getStartTime() != null) {
            return timeRange.getStartTime().format(PERIOD_VALUE_FORMATTER) + AFTER_SUFFIX;
        }
        if (timeRange.getEndTime() != null) {
            return timeRange.getEndTime().format(PERIOD_VALUE_FORMATTER) + BEFORE_SUFFIX;
        }
        return ALL_APPROVED_RECORDS;
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
            case TEACHING_DESIGN -> "\u6559\u5b66\u8bbe\u8ba1";
            case CLASSROOM_ORGANIZATION -> "\u8bfe\u5802\u7ec4\u7ec7";
            case TEACHING_CONTENT -> "\u6559\u5b66\u5185\u5bb9";
            case INTERACTION_FEEDBACK -> "\u4e92\u52a8\u53cd\u9988";
            case TEACHING_EFFECTIVENESS -> "\u6559\u5b66\u6548\u679c";
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
        return String.join(TEXT_SEPARATOR, parts);
    }

    private AnalyticsReportVo persistReport(Long leaderId, AnalyticsReportVo report) {
        RadarReport radarReport = new RadarReport();
        radarReport.setLeaderId(leaderId);
        radarReport.setTeacherName(report.getTeacherName());
        radarReport.setPeriodType(report.getPeriodType());
        radarReport.setPeriodValue(report.getPeriodValue());
        radarReport.setStartTime(report.getStartTime());
        radarReport.setEndTime(report.getEndTime());
        radarReport.setSampleCount(report.getSampleCount());
        radarReport.setRadarJson(toRadarJson(report.getRadarChart()));
        radarReport.setStrengthSummary(report.getStrengthSummary());
        radarReport.setWeaknessSummary(report.getWeaknessSummary());
        radarReport.setConclusion(report.getConclusion());
        analyticsMapper.insert(radarReport);
        return findSavedReportDetail(radarReport.getId());
    }

    private AnalyticsReportVo toVo(RadarReport radarReport) {
        return AnalyticsReportVo.builder()
                .id(radarReport.getId())
                .teacherName(radarReport.getTeacherName())
                .periodType(radarReport.getPeriodType())
                .periodValue(radarReport.getPeriodValue())
                .startTime(radarReport.getStartTime())
                .endTime(radarReport.getEndTime())
                .generatedAt(radarReport.getGeneratedAt())
                .sampleCount(radarReport.getSampleCount())
                .radarChart(parseRadarChart(radarReport.getRadarJson()))
                .strengthSummary(radarReport.getStrengthSummary())
                .weaknessSummary(radarReport.getWeaknessSummary())
                .conclusion(radarReport.getConclusion())
                .build();
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

    private AnalyticsReportVo.RadarChartVo parseRadarChart(String radarJson) {
        if (radarJson == null || radarJson.isBlank()) {
            return null;
        }
        try {
            return AnalyticsReportVo.RadarChartVo.builder()
                    .indicators(parseIndicators(radarJson))
                    .values(parseValues(radarJson))
                    .build();
        } catch (RuntimeException ex) {
            throw new BusinessException(500, "\u96f7\u8fbe\u56fe\u89e3\u6790\u5931\u8d25");
        }
    }

    private List<AnalyticsReportVo.IndicatorVo> parseIndicators(String radarJson) {
        int indicatorsStart = radarJson.indexOf(INDICATORS_KEY);
        int valuesStart = radarJson.indexOf(VALUES_KEY);
        String indicatorsJson = radarJson.substring(indicatorsStart + INDICATORS_KEY.length(), valuesStart - 1).trim();
        String normalized = indicatorsJson.substring(1, indicatorsJson.length() - 1);
        List<AnalyticsReportVo.IndicatorVo> indicators = new ArrayList<>();
        if (normalized.isBlank()) {
            return indicators;
        }
        String[] items = normalized.split("\\},\\{");
        for (String item : items) {
            String cleaned = item.replace("{", "").replace("}", "");
            String[] pairs = cleaned.split(",");
            String name = "";
            int max = 5;
            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length != 2) {
                    continue;
                }
                String key = keyValue[0].replace("\"", "").trim();
                String value = keyValue[1].replace("\"", "").trim();
                if ("name".equals(key)) {
                    name = value;
                }
                if ("max".equals(key)) {
                    max = Integer.parseInt(value);
                }
            }
            indicators.add(AnalyticsReportVo.IndicatorVo.builder()
                    .name(name)
                    .max(max)
                    .build());
        }
        return indicators;
    }

    private List<BigDecimal> parseValues(String radarJson) {
        int valuesStart = radarJson.indexOf(VALUES_KEY);
        String valuesJson = radarJson.substring(valuesStart + VALUES_KEY.length()).trim();
        String normalized = valuesJson.substring(1, valuesJson.length() - 2);
        List<BigDecimal> values = new ArrayList<>();
        if (normalized.isBlank()) {
            return values;
        }
        String[] items = normalized.split(",");
        for (String item : items) {
            values.add(new BigDecimal(item.trim()));
        }
        return values;
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static class TimeRange {

        private final LocalDateTime startTime;
        private final LocalDateTime endTime;

        private TimeRange(LocalDateTime startTime, LocalDateTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }

        private LocalDateTime getStartTime() {
            return startTime;
        }

        private LocalDateTime getEndTime() {
            return endTime;
        }
    }
}
