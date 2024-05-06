package ti.gateway.dashboard.domain.overview;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Features {
    private String tracing;
    private String metrics;
    private boolean accessLog;
    private boolean hub;
}
