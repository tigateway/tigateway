package ti.gateway.admin.domain.overview;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProtocolStats {
    private ComponentStats routers;
    private ComponentStats services;
    private ComponentStats middlewares;
}
