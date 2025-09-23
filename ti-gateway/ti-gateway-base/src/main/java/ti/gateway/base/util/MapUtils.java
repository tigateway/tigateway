package ti.gateway.base.util;

import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUtils {

    /**
     * MultiValueMap to HashMap
     *
     * @param m MultiValueMap
     * @return HashMap
     */
    public static Map<String, String> convertMultiToRegularMap(MultiValueMap<String, String> m) {
        if (m == null) {
            return new HashMap<>();
        }
        Map<String, String> map = new HashMap<>(m.size());
        for (Map.Entry<String, List<String>> entry : m.entrySet()) {
            StringBuilder sb = new StringBuilder();
            for (String s : entry.getValue()) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(s);
            }
            map.put(entry.getKey(), sb.toString());
        }
        return map;
    }

}
