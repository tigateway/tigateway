package ti.gateway.operator.springcloudgateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Clock;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

class LocalVaultFileReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalVaultFileReader.class);
    private final Path secretsFile;
    private final Clock clock;
    private final Map<String, Map<String, Object>> knownSecrets = new ConcurrentHashMap<>();
    private long lastRefreshTime;
    private WatchService watchService;
    private final ExecutorService watcherThread = Executors.newSingleThreadExecutor();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    LocalVaultFileReader(Path secretsFilePath, Clock jwtFileLocatorClock) {
        this.secretsFile = secretsFilePath;
        this.clock = jwtFileLocatorClock;
        this.lastRefreshTime = this.clock.millis();

        try {
            if (Files.exists(this.secretsFile, new LinkOption[0])) {
                this.reloadSecrets(this.secretsFile);
            } else {
                LOGGER.info(secretsFilePath + " not found");
            }

            this.startWatchingForChanges(this.secretsFile);
        } catch (IOException var4) {
            LOGGER.error("Could not initialize WatchService", var4);
        }

    }

    private void startWatchingForChanges(Path keysFile) throws IOException {
        Path parentPath = keysFile.toAbsolutePath().getParent();
        LOGGER.info("Starting to watch {} folder for changes", parentPath);
        this.watchService = FileSystems.getDefault().newWatchService();
        parentPath.register(this.watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
        this.watcherThread.submit(this::watchFileChanges);
    }

    private void watchFileChanges() {
        while(true) {
            try {
                WatchKey key;
                if ((key = this.watchService.take()) != null) {
                    LOGGER.trace("Detected events");

                    for(Iterator<WatchEvent<?>> var2 = key.pollEvents().iterator(); var2.hasNext(); this.lastRefreshTime = this.clock.millis()) {
                        WatchEvent<?> event = (WatchEvent<?>)var2.next();
                        if (StandardWatchEventKinds.ENTRY_MODIFY.equals(event.kind()) || StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
                            LOGGER.debug("Secrets file changed, reloading");
                            this.reloadSecrets(this.secretsFile);
                        }

                        if (StandardWatchEventKinds.ENTRY_DELETE.equals(event.kind())) {
                            LOGGER.debug("Secrets file deleted, emptying keys");
                            this.knownSecrets.clear();
                        }
                    }

                    key.reset();
                    continue;
                }
            } catch (InterruptedException var4) {
                Thread.currentThread().interrupt();
                LOGGER.error("Failed to watch secrets file, interrupted", var4);
            }

            return;
        }
    }

    private void reloadSecrets(Path keysFile) {
        try {
            String secretIdPattern = "[\\w-]+";
            Map<String, Map<String, Object>> newKeys = (new Scanner(keysFile)).findAll(".*\\{[\\s\\S]*?(?=" + System.lineSeparator() + "[\\w-]+\\{|$)")
                    .map(MatchResult::group)
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map((line) -> {
                        return this.parseLine(line);
                    })
                    .collect(Collectors.toMap((entry) -> {
                        return (String)entry.get("id");
                    }, (entry) -> {
                        return entry;
                    }));
            LOGGER.info("Reloading LocalFileJwtKeysLocator keys: {} keys loaded", newKeys.size());
            this.knownSecrets.clear();
            this.knownSecrets.putAll(newKeys);
        } catch (IOException var4) {
            LOGGER.error("Failed to reload secrets file", var4);
        }

    }

    private Map<String, Object> parseLine(String line) {
        int split = line.indexOf("{");
        String id = line.substring(0, split);
        String json = line.substring(split);

        try {
            Map<String, Object> parsedSecret = (Map)this.jsonMapper.readValue(json, HashMap.class);
            parsedSecret.put("id", id);
            return parsedSecret;
        } catch (JsonProcessingException var6) {
            LOGGER.warn("Could not parse line, invalid format: " + line);
            throw new RuntimeException(var6);
        }
    }

    public Map<String, Object> getSecret(String id) {
        return this.knownSecrets.get(id);
    }

    public Set<String> getIds() {
        return this.knownSecrets.keySet();
    }

    public long lastRefreshTime() {
        return this.lastRefreshTime;
    }
}
