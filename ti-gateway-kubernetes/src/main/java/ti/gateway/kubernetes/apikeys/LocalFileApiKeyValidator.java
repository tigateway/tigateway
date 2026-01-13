package ti.gateway.kubernetes.apikeys;

import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
@ApiKeyRequired
public class LocalFileApiKeyValidator implements ApiKeyValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileApiKeyValidator.class);
    @SuppressWarnings("unused")
    private static final String SEPARATOR = ",";
    private final Path keysFile;
    private final Map<String, String> knownKeys;
    private final WatchService watchService = FileSystems.getDefault().newWatchService();
    private final ExecutorService watcherThread = Executors.newSingleThreadExecutor();

    public LocalFileApiKeyValidator(@Value("${apiKey.keysFilePath}") String keysFilePath) throws IOException {
        this.keysFile = Path.of(keysFilePath);
        this.knownKeys = new HashMap<>();
        this.reloadKeys(this.keysFile);
        this.startWatchingForChanges(this.keysFile.getParent());
    }

    private void startWatchingForChanges(Path keysFileDir) throws IOException {
        LOGGER.info("Starting to watch {} folder for changes", keysFileDir.toAbsolutePath());
        keysFileDir.register(this.watchService, StandardWatchEventKinds.ENTRY_MODIFY);
        this.watcherThread.submit(this::watchKeysFileChange);
    }

    private void watchKeysFileChange() {
        while (true) {
            try {
                WatchKey key;
                if ((key = this.watchService.take()) != null) {
                    LOGGER.trace("Detected events");
                    Iterator<WatchEvent<?>> watchEventIterator = key.pollEvents().iterator();

                    while (watchEventIterator.hasNext()) {
                        WatchEvent<?> event = (WatchEvent<?>) watchEventIterator.next();
                        if (StandardWatchEventKinds.ENTRY_MODIFY.equals(event.kind())) {
                            LOGGER.debug("API Keys file changed, reloading");
                            this.reloadKeys(this.keysFile);
                        }
                    }

                    key.reset();
                    continue;
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                LOGGER.error("Failed to watch API keys file, interrupted", exception);
            }

            return;
        }
    }

    private void reloadKeys(Path keysFile) {
        try {
            Map<String, String> newKeys = Files.lines(keysFile)
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .filter((line) -> {
                        return line.contains(",");
                    }).map((line) -> {
                        return line.split(",");
                    })
                    .collect(Collectors.toMap((values) -> {
                            return values[0];
                        }, (values) -> {
                            return values[1];
                    }));

            LOGGER.info("Reloading API keys: {} keys loaded", newKeys.size());
            this.knownKeys.clear();
            this.knownKeys.putAll(newKeys);
        } catch (IOException exception) {
            LOGGER.error("Failed to watch API keys file!", exception);
        }
    }

    @Override
    public Mono<Boolean> keyIsValid(String token) {
        LOGGER.trace("Validating API ke");
        String selector = this.getSelector(token);
        String verifier = this.getVerifier(token);
        String hashedApiKey = selector != null ? (String) this.knownKeys.get(selector) : null;
        return Mono.just(hashedApiKey != null && this.hashKey(verifier).equals(hashedApiKey));
    }

    private String getSelector(String token) {
        return token != null && token.length() == 64 ? token.substring(0, 32) : null;
    }

    private String getVerifier(String token) {
        return token != null && token.length() == 64 ? token.substring(32) : null;
    }

    private String hashKey(String apiKey) {
        MessageDigest messageDigest = new SHA256.Digest();
        byte[] digest = messageDigest.digest(apiKey.getBytes(StandardCharsets.UTF_8));
        return Hex.toHexString(digest);
    }

}
