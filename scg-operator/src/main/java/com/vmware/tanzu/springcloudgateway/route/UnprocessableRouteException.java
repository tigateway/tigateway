package com.vmware.tanzu.springcloudgateway.route;

public class UnprocessableRouteException extends RuntimeException {
    public UnprocessableRouteException() {
    }

    public UnprocessableRouteException(String message, Throwable cause) {
        super(message, cause);
    }
}
