package com.edu.tobserver.common.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void shouldCreateSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("payload");

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("success");
        assertThat(response.getData()).isEqualTo("payload");
    }

    @Test
    void shouldCreateFailureResponseWithoutData() {
        ApiResponse<Void> response = ApiResponse.failure(400, "bad request");

        assertThat(response.getCode()).isEqualTo(400);
        assertThat(response.getMessage()).isEqualTo("bad request");
        assertThat(response.getData()).isNull();
    }
}
