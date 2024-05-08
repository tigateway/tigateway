package ti.gateway.admin.domain.overview;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentStats {
    private int total;
    private int warnings;
    private int errors;
}
