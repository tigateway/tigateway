package ti.gateway.operator.springcloudgateway.openapi.extensions;

import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import org.springframework.stereotype.Component;

@Component
public class BearerAuthSecuritySchemeComponentCustomizer extends AbstractSecuritySchemeComponentCustomizer {
    private static final String SECURITY_NAME = "BearerAuth";
    private static final String SECURITY_SCHEME = "bearer";
    private static final Type SECURITY_TYPE;

    public BearerAuthSecuritySchemeComponentCustomizer() {
        super("BearerAuth", "bearer", SECURITY_TYPE);
    }

    static {
        SECURITY_TYPE = Type.HTTP;
    }
}
