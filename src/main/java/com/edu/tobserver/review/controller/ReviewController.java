package com.edu.tobserver.review.controller;

import com.edu.tobserver.common.api.ApiResponse;
import com.edu.tobserver.record.vo.ObservationRecordVo;
import com.edu.tobserver.review.dto.RejectRequest;
import com.edu.tobserver.review.service.ReviewService;
import com.edu.tobserver.review.vo.ReviewListItemVo;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ApiResponse<List<ReviewListItemVo>> list() {
        return ApiResponse.success(reviewService.findList());
    }

    @GetMapping("/{recordId}")
    public ApiResponse<ObservationRecordVo> detail(@PathVariable Long recordId) {
        return ApiResponse.success(reviewService.findDetail(recordId));
    }

    @PostMapping("/{recordId}/approve")
    public ApiResponse<ObservationRecordVo> approve(@PathVariable Long recordId) {
        return ApiResponse.success(reviewService.approve(recordId));
    }

    @PostMapping("/{recordId}/reject")
    public ApiResponse<ObservationRecordVo> reject(@PathVariable Long recordId,
                                                   @Valid @RequestBody RejectRequest request) {
        return ApiResponse.success(reviewService.reject(recordId, request.getReason()));
    }
}
