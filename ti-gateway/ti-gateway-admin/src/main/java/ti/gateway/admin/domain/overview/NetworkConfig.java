package ti.gateway.admin.domain.overview;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NetworkConfig {
    private ProtocolStats http;
    private ProtocolStats tcp;
    private ProtocolStats udp;
    private Features features;
    private List<String> providers;
}
