package ti.gateway.admin.service;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @version 1.0
 * @date 2024/5/6 23:39
 */
@Data
public class Filter {
    @JsonProperty("filter_type")
    private String filterType;
    private int parts;
    private int order;

    public Filter() {
    }

    @JsonCreator
    public Filter(String rawFilter) {
        // Example rawFilter: "[[StripPrefix parts = 1], order = 1]"
        // Parsing logic needs to extract relevant parts
        try {
            this.filterType = rawFilter.split(" ")[0].replaceAll("[\\[\\]]", "");
            this.parts = Integer.parseInt(rawFilter.split("parts = ")[1].split("]")[0].trim());
            this.order = Integer.parseInt(rawFilter.split("order = ")[1].trim().replaceAll("]", ""));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid filter format");
        }
    }

}