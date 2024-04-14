package ti.gateway.dashboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ti.gateway.dashboard.domain.ServiceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 02:09
 */
@RestController
public class HttpServicesController {

    @GetMapping("/api/http/services")
    public List<ServiceInfo> getHttpServices() {
        // Create a list to store ServiceInfo objects
        List<ServiceInfo> services = new ArrayList<>();

        // Create ServiceInfo objects with sample data
        // Replace this with your own data
        ServiceInfo service1 = new ServiceInfo();
        service1.setName("bar@myprovider");
        // Set other properties for service1

        ServiceInfo service2 = new ServiceInfo();
        service2.setName("baz@myprovider");
        // Set other properties for service2

        // Add ServiceInfo objects to the list
        services.add(service1);
        services.add(service2);

        // Return the list of ServiceInfo objects
        return services;
    }


}
