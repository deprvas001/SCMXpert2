package com.example.scmxpert.model.updateEventModel;

import com.example.scmxpert.model.ApiResponse;
import com.example.scmxpert.model.loginModel.LoginResponse;

public class UpdateApiResponse {
    public ApiResponse response;
    private Throwable error;

    public UpdateApiResponse(ApiResponse response) {
        this.response = response;
        this.error = null;
    }

    public UpdateApiResponse(Throwable error) {
        this.error = error;
        this.response = null;
    }

    public ApiResponse getResponse() {
        return response;
    }

    public void setResponse(ApiResponse response) {
        this.response = response;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}
