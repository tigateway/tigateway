package com.vmware.tanzu.springcloudgateway.route;

import io.kubernetes.client.openapi.ApiException;

public class UriBuildingException extends Exception {
    public UriBuildingException(String message, ApiException e) {
        super(message, e);
    }
}
