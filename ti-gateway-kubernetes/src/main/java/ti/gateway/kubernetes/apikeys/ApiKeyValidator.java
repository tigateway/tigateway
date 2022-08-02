package ti.gateway.kubernetes.apikeys;

import reactor.core.publisher.Mono;

public interface ApiKeyValidator {
    Mono<Boolean> keyIsValid(String apiKey);
}
