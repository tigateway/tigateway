package ti.gateway.admin.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @date 2024/5/6 23:38
 */

@Data
public class Route {
    @JsonProperty("route_id")
    private String routeId;
    private String uri;
    private List<String> predicates;
    private List<String> filters;
    private int order;

    public Route() {
    }

    public Route(String routeId, String uri, List<String> predicates, List<String> filters, int order) {
        this.routeId = routeId;
        this.uri = uri;
        this.predicates = predicates;
        this.filters = filters;
        this.order = order;
    }

    public List<Filter> getFilterRaws() {
        return filters.stream()
                .map(Filter::new)
                .collect(Collectors.toList());
    }

}

