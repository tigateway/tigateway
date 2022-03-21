package com.vmware.tanzu.springcloudgateway.helper;

import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGateway;
import org.springframework.util.StringUtils;

public class GroupIdGetter {
    public GroupIdGetter() {
    }

    public static String getGroupId(V1SpringCloudGateway gateway) {
        if (gateway.getSpec() != null && gateway.getSpec().getApi() != null) {
            if (StringUtils.hasText(gateway.getSpec().getApi().getGroupId())) {
                return gateway.getSpec().getApi().getGroupId();
            }

            if (StringUtils.hasText(gateway.getSpec().getApi().getTitle())) {
                return gateway.getSpec().getApi().getTitle().replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();
            }
        }

        return gateway.getMetadata().getName();
    }
}

