package com.edu.tobserver.analytics.controller;

import com.edu.tobserver.analytics.dto.AnalyticsGenerateRequest;
import com.edu.tobserver.analytics.service.AnalyticsService;
import com.edu.tobserver.analytics.vo.AnalyticsReportVo;
import com.edu.tobserver.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping("/generate")
    public ApiResponse<AnalyticsReportVo> generate(@Valid @RequestBody AnalyticsGenerateRequest request) {
        return ApiResponse.success(analyticsService.generate(request));
    }
}
