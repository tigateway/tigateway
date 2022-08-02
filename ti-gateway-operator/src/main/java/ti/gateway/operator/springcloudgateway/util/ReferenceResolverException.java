package ti.gateway.operator.springcloudgateway.util;

public class ReferenceResolverException extends RuntimeException {
    public ReferenceResolverException() {
    }

    public ReferenceResolverException(String message) {
        super(message);
    }

    public ReferenceResolverException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReferenceResolverException(Throwable cause) {
        super(cause);
    }
}
