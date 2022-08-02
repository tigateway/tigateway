package ti.gateway.kubernetes.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.*;
import java.time.Clock;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;

@Component
@JwtKeyEnabled
public class LocalFileJwtKeysLocator {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileJwtKeysLocator.class);
    private static final String KEY_SEPARATOR = ",";
    private final Path keysFile;
    private final Clock clock;
    private final Map<String, Key> knownKeys = new ConcurrentHashMap<>();
    private long lastRefreshTime;
    private WatchService watchService;
    private final ExecutorService watcherThread = Executors.newSingleThreadExecutor();

    LocalFileJwtKeysLocator(@Value("${jwtKey.keysFilePath}") String keysFilePath, Clock jwtFileLocatorClock) {
        this.keysFile = Path.of(keysFilePath);
        this.clock = jwtFileLocatorClock;
        this.lastRefreshTime = this.clock.millis();

        try {
            if (Files.exists(this.keysFile, new LinkOption[0])) {
                this.reloadKeys(this.keysFile);
            } else {
                LOGGER.info(keysFilePath + " not found");
            }

            this.startWatchingForChanges(this.keysFile);
        } catch (IOException exception) {
            LOGGER.error("Could not initialize WatchService", exception);
        }

    }

    private void startWatchingForChanges(Path keysFile) throws IOException {
        Path parentPath = keysFile.toAbsolutePath().getParent();
        LOGGER.info("Starting to watch {} folder for changes", parentPath);
        this.watchService = FileSystems.getDefault().newWatchService();
        parentPath.register(this.watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
        this.watcherThread.submit(this::watchKeysFileChange);
    }

    private void watchKeysFileChange() {
        while(true) {
            try {
                WatchKey key;
                if ((key = this.watchService.take()) != null) {
                    LOGGER.trace("Detected events");

                    for(Iterator iterator = key.pollEvents().iterator(); iterator.hasNext(); this.lastRefreshTime = this.clock.millis()) {
                        WatchEvent<?> event = (WatchEvent) iterator.next();
                        if (StandardWatchEventKinds.ENTRY_MODIFY.equals(event.kind()) || StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
                            LOGGER.debug("JWT keys file changed, reloading");
                            this.reloadKeys(this.keysFile);
                        }

                        if (StandardWatchEventKinds.ENTRY_DELETE.equals(event.kind())) {
                            LOGGER.debug("JWT keys file deleted, emptying keys");
                            this.knownKeys.clear();
                        }
                    }

                    key.reset();
                    continue;
                }
            } catch (InterruptedException var4) {
                Thread.currentThread().interrupt();
                LOGGER.error("Failed to watch JWT keys file, interrupted", var4);
            }

            return;
        }
    }

    private void reloadKeys(Path keysFile) {
        try {
            Map<String, LocalFileJwtKeysLocator.Key> newKeys = (Map) (new Scanner(keysFile))
                    .findAll(".*,.*,.*[\\s\\S]*?(?=" + System.lineSeparator() + ".*?,|$)")
                    .map(MatchResult::group)
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .filter((line) -> {
                        return line.contains(",");
                    })
                    .map((line) -> {
                        return line.split(",");
                    })
                    .collect(Collectors.toMap((entry) -> {
                        return entry[0];
                    }, (entry) -> {
                        return new LocalFileJwtKeysLocator.Key(entry[1], entry[2]);
                    }));

            LOGGER.info("Reloading LocalFileJwtKeysLocator keys: {} keys loaded", newKeys.size());
            this.knownKeys.clear();
            this.knownKeys.putAll(newKeys);
        } catch (IOException var3) {
            LOGGER.error("Failed to reload JWT keys file!", var3);
        }

    }

    public LocalFileJwtKeysLocator.Key getKey(String keyId) {
        return (LocalFileJwtKeysLocator.Key)this.knownKeys.get(keyId);
    }

    public Set<String> getKeyIds() {
        return this.knownKeys.keySet();
    }

    public long lastRefreshTime() {
        return this.lastRefreshTime;
    }

    static class Key {
        private final String alg;
        private final String key;

        Key(String alg, String key) {
            this.alg = alg;
            this.key = key;
        }

        public String getAlg() {
            return this.alg;
        }

        public String getKey() {
            return this.key;
        }
    }
}
