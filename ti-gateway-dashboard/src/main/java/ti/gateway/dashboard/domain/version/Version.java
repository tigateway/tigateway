package ti.gateway.dashboard.domain.version;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Version {
    private String version;
    private String codename;
    private ZonedDateTime startDate;
}
