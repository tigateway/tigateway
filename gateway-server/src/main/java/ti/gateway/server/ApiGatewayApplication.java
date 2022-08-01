package ti.gateway.server;

@SpringBootApplication(scanBasePackages = {"ti.gateway"})
@MapperScan(value = "com.risen.base.api.gateway.storage.db.mapper")
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
