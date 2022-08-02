package ti.gateway.kubernetes.session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.Offloadable;
import com.hazelcast.map.EntryProcessor;

import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.session.MapSession;

class SessionUpdateEntryProcessor implements EntryProcessor<String, String, Boolean>, Offloadable {
    private static final long serialVersionUID = -6429700235054972734L;
    private static final Logger log = LoggerFactory.getLogger(SessionUpdateEntryProcessor.class);
    private static final ObjectMapper objectMapper = SessionConfiguration.HazelcastReactiveSessionConfiguration.getSessionObjectMapper();
    private Instant lastAccessedTime;
    private Duration maxInactiveInterval;
    private Map<String, Object> delta;

    SessionUpdateEntryProcessor() {
    }

    public Boolean process(Entry<String, String> entry) {
        String strValue = (String) entry.getValue();

        MapSession value;
        try {
            value = (MapSession) objectMapper.readValue(strValue, MapSession.class);
        } catch (JsonProcessingException var7) {
            log.error("Failed to deserialize a session for entry processing", var7);
            throw new RuntimeException(var7);
        }

        if (value == null) {
            return Boolean.FALSE;
        } else {
            if (this.lastAccessedTime != null) {
                value.setLastAccessedTime(this.lastAccessedTime);
            }

            if (this.maxInactiveInterval != null) {
                value.setMaxInactiveInterval(this.maxInactiveInterval);
            }

            if (this.delta != null) {
                Iterator var4 = this.delta.entrySet().iterator();

                while (var4.hasNext()) {
                    Entry<String, Object> attribute = (Entry) var4.next();
                    if (attribute.getValue() != null) {
                        value.setAttribute((String) attribute.getKey(), attribute.getValue());
                    } else {
                        value.removeAttribute((String) attribute.getKey());
                    }
                }
            }

            try {
                entry.setValue(objectMapper.writeValueAsString(value));
            } catch (JsonProcessingException var6) {
                log.error("Failed to serialize a processed session entry into JSON", var6);
                throw new RuntimeException(var6);
            }

            return Boolean.TRUE;
        }
    }

    public String getExecutorName() {
        return "hz:offloadable";
    }

    void setLastAccessedTime(Instant lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    void setMaxInactiveInterval(Duration maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    void setDelta(Map<String, Object> delta) {
        this.delta = delta;
    }
}
