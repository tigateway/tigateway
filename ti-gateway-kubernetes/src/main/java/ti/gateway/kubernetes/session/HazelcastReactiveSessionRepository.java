package ti.gateway.kubernetes.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.session.FlushMode;
import org.springframework.session.MapSession;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.Session;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class HazelcastReactiveSessionRepository implements ReactiveSessionRepository<HazelcastReactiveSessionRepository.HazelcastSession> {
    public static final String DEFAULT_SESSION_MAP_NAME = "spring:session:sessions";
    private static final Logger log = LoggerFactory.getLogger(HazelcastReactiveSessionRepository.class);
    private final HazelcastInstance hazelcastInstance;
    private Integer defaultMaxInactiveInterval;
    private String sessionMapName = "spring:session:sessions";
    private FlushMode flushMode;
    private IMap<String, String> sessions;
    private final ObjectMapper objectMapper;

    public HazelcastReactiveSessionRepository(HazelcastInstance hazelcastInstance, ObjectMapper objectMapper) {
        this.flushMode = FlushMode.ON_SAVE;
        Assert.notNull(hazelcastInstance, "HazelcastInstance must not be null");
        this.hazelcastInstance = hazelcastInstance;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        this.sessions = this.hazelcastInstance.getMap(this.sessionMapName);
    }

    public void setDefaultMaxInactiveInterval(Integer defaultMaxInactiveInterval) {
        this.defaultMaxInactiveInterval = defaultMaxInactiveInterval;
    }

    public void setSessionMapName(String sessionMapName) {
        Assert.hasText(sessionMapName, "Map name must not be empty");
        this.sessionMapName = sessionMapName;
    }

    public void setFlushMode(FlushMode flushMode) {
        Assert.notNull(flushMode, "FlushMode cannot be null");
        this.flushMode = flushMode;
    }

    public Mono<HazelcastReactiveSessionRepository.HazelcastSession> createSession() {
        HazelcastReactiveSessionRepository.HazelcastSession result = new HazelcastReactiveSessionRepository.HazelcastSession();
        if (this.defaultMaxInactiveInterval != null) {
            result.setMaxInactiveInterval(Duration.ofSeconds((long)this.defaultMaxInactiveInterval));
        }

        return Mono.defer(() -> {
            return Mono.just(result);
        });
    }

    public Mono<Void> save(HazelcastReactiveSessionRepository.HazelcastSession session) {
        Mono<Void> result = Mono.empty();
        if (session.isNew) {
            if (log.isDebugEnabled()) {
                log.debug("New: " + session.getId());
            }

            result = this.serializeSession(session).flatMap((jsonSession) -> {
                return Mono.fromCompletionStage(this.sessions.setAsync(session.getId(), jsonSession, session.getMaxInactiveInterval().getSeconds(), TimeUnit.SECONDS));
            });
        } else if (session.sessionIdChanged) {
            if (log.isDebugEnabled()) {
                String var10001 = session.originalId;
                log.debug("ID: " + var10001 + " -> " + session.getId());
            }

            String originalId = session.originalId;
            session.originalId = session.getId();
            result = this.serializeSession(session).flatMap((jsonSession) -> {
                Mono<Void> removeMono = Mono.fromCompletionStage(this.sessions.removeAsync(originalId)).then();
                Mono<Void> setMono = Mono.fromCompletionStage(this.sessions.setAsync(session.getId(), jsonSession, session.getMaxInactiveInterval().getSeconds(), TimeUnit.SECONDS)).then();
                return Flux.merge(removeMono, setMono).then();
            });
        } else if (session.hasChanges()) {
            if (log.isDebugEnabled()) {
                log.debug("Changed: " + session.getId());
            }

            SessionUpdateEntryProcessor entryProcessor = new SessionUpdateEntryProcessor();
            if (session.lastAccessedTimeChanged) {
                entryProcessor.setLastAccessedTime(session.getLastAccessedTime());
            }

            if (session.maxInactiveIntervalChanged) {
                entryProcessor.setMaxInactiveInterval(session.getMaxInactiveInterval());
            }

            if (!session.delta.isEmpty()) {
                entryProcessor.setDelta(session.delta);
            }

            result = Mono.fromCompletionStage(this.sessions.submitToKey(session.getId(), entryProcessor)).then();
        }

        Objects.requireNonNull(session);
        return result.then(Mono.fromRunnable(session::clearChangeFlags));
    }

    private Mono<String> serializeSession(HazelcastReactiveSessionRepository.HazelcastSession session) {
        return Mono.fromCallable(() -> {
            return this.objectMapper.writeValueAsString(session.getDelegate());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<MapSession> deserializeSession(String jsonSession) {
        return Mono.fromCallable(() -> {
            return (MapSession)this.objectMapper.readValue(jsonSession, MapSession.class);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<HazelcastReactiveSessionRepository.HazelcastSession> findById(String id) {
        return Mono.defer(() -> {
            return Mono.fromCompletionStage(this.sessions.getAsync(id));
        }).flatMap((saved) -> {
            return this.deserializeSession(saved).flatMap((session) -> {
                return session.isExpired() ? this.deleteById(session.getId()).then(Mono.empty()) : Mono.just(new HazelcastReactiveSessionRepository.HazelcastSession(session));
            });
        });
    }

    public Mono<Void> deleteById(String id) {
        return Mono.fromCompletionStage(this.sessions.removeAsync(id)).then();
    }

    final class HazelcastSession implements Session {
        private final MapSession delegate;
        private boolean isNew;
        private boolean sessionIdChanged;
        private boolean lastAccessedTimeChanged;
        private boolean maxInactiveIntervalChanged;
        private String originalId;
        private Map<String, Object> delta;

        HazelcastSession() {
            this(new MapSession());
            this.isNew = true;
            this.flushImmediateIfNecessary();
        }

        HazelcastSession(MapSession cached) {
            this.delta = new HashMap<>();
            Assert.notNull(cached, "MapSession cannot be null");
            this.delegate = cached;
            this.originalId = cached.getId();
        }

        public void setLastAccessedTime(Instant lastAccessedTime) {
            this.delegate.setLastAccessedTime(lastAccessedTime);
            this.lastAccessedTimeChanged = true;
            this.flushImmediateIfNecessary();
        }

        public boolean isExpired() {
            return this.delegate.isExpired();
        }

        public Instant getCreationTime() {
            return this.delegate.getCreationTime();
        }

        public String getId() {
            return this.delegate.getId();
        }

        public String changeSessionId() {
            String newSessionId = this.delegate.changeSessionId();
            this.sessionIdChanged = true;
            return newSessionId;
        }

        public Instant getLastAccessedTime() {
            return this.delegate.getLastAccessedTime();
        }

        public void setMaxInactiveInterval(Duration interval) {
            this.delegate.setMaxInactiveInterval(interval);
            this.maxInactiveIntervalChanged = true;
            this.flushImmediateIfNecessary();
        }

        public Duration getMaxInactiveInterval() {
            return this.delegate.getMaxInactiveInterval();
        }

        public <T> T getAttribute(String attributeName) {
            return this.delegate.getAttribute(attributeName);
        }

        public Set<String> getAttributeNames() {
            return this.delegate.getAttributeNames();
        }

        public void setAttribute(String attributeName, Object attributeValue) {
            this.delegate.setAttribute(attributeName, attributeValue);
            this.delta.put(attributeName, attributeValue);
            this.flushImmediateIfNecessary();
        }

        public void removeAttribute(String attributeName) {
            this.delegate.removeAttribute(attributeName);
            this.delta.put(attributeName, (Object)null);
            this.flushImmediateIfNecessary();
        }

        MapSession getDelegate() {
            return this.delegate;
        }

        boolean hasChanges() {
            return this.lastAccessedTimeChanged || this.maxInactiveIntervalChanged || !this.delta.isEmpty();
        }

        void clearChangeFlags() {
            this.isNew = false;
            this.lastAccessedTimeChanged = false;
            this.sessionIdChanged = false;
            this.maxInactiveIntervalChanged = false;
            this.delta.clear();
        }

        private void flushImmediateIfNecessary() {
            if (HazelcastReactiveSessionRepository.this.flushMode == FlushMode.IMMEDIATE) {
                HazelcastReactiveSessionRepository.this.save(this).subscribe();
            }

        }
    }
}
