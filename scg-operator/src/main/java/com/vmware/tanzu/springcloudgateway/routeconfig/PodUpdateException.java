package com.vmware.tanzu.springcloudgateway.routeconfig;

public class PodUpdateException extends Throwable {
    public PodUpdateException(String url, int code, String message) {
        super("Pod update failed, request to " + url + " failed. Response code " + code + ", message " + message);
    }

    public PodUpdateException(String message, Exception e) {
        super(message, e);
    }
}
