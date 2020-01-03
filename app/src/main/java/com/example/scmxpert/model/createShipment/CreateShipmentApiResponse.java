package com.example.scmxpert.model.createShipment;

import com.example.scmxpert.model.forgotModel.ForgotResponse;

public class CreateShipmentApiResponse {

    public CreateShipmentResponse response;
    private Throwable error;

    public CreateShipmentApiResponse(CreateShipmentResponse response) {
        this.response = response;
        this.error = null;
    }

    public CreateShipmentApiResponse(Throwable error) {
        this.error = error;
        this.response = null;
    }

    public CreateShipmentResponse getResponse() {
        return response;
    }

    public void setResponse(CreateShipmentResponse response) {
        this.response = response;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}
