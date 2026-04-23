package com.edu.tobserver.analytics.controller;

import com.edu.tobserver.analytics.dto.AnalyticsGenerateRequest;
import com.edu.tobserver.analytics.service.AnalyticsService;
import com.edu.tobserver.analytics.vo.AnalyticsReportVo;
import com.edu.tobserver.analytics.vo.AnalyticsTeacherOptionVo;
import com.edu.tobserver.analytics.vo.SavedAnalyticsReportItemVo;
import com.edu.tobserver.common.api.ApiResponse;
import com.edu.tobserver.common.api.PageResult;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/teachers")
    public ApiResponse<List<AnalyticsTeacherOptionVo>> teachers(@RequestParam(required = false) String keyword) {
        return ApiResponse.success(analyticsService.findTeacherOptions(keyword));
    }

    @PostMapping("/generate")
    public ApiResponse<AnalyticsReportVo> generate(@RequestBody AnalyticsGenerateRequest request) {
        return ApiResponse.success(analyticsService.generate(request));
    }

    @PostMapping("/reports")
    public ApiResponse<AnalyticsReportVo> save(@RequestBody AnalyticsGenerateRequest request) {
        return ApiResponse.success(analyticsService.save(request));
    }

    @GetMapping("/reports")
    public ApiResponse<PageResult<SavedAnalyticsReportItemVo>> reports(@RequestParam(required = false) Integer pageNum,
                                                                       @RequestParam(required = false) Integer pageSize) {
        return ApiResponse.success(analyticsService.findSavedReports(pageNum, pageSize));
    }

    @GetMapping("/reports/{reportId}")
    public ApiResponse<AnalyticsReportVo> reportDetail(@PathVariable Long reportId) {
        return ApiResponse.success(analyticsService.findSavedReportDetail(reportId));
    }
}
