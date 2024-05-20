package com.attica.athens.domain.common;

import com.attica.athens.domain.common.advice.ErrorResponse;

public class ApiUtil {

    public static <T> ApiResponse success(T response) {
        return new ApiResponse(true, response, null);
    }

    public static ApiResponse failure(ErrorResponse error) {
        return new ApiResponse(false, null, error);
    }
}
