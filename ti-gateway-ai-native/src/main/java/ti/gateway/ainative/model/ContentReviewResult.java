package ti.gateway.ainative.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 内容审核结果模型
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentReviewResult {
    
    @JsonProperty("blocked")
    private boolean blocked;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("confidence")
    private double confidence;
    
    @JsonProperty("categories")
    private List<String> categories;
    
    @JsonProperty("details")
    private Map<String, Object> details;
    
    @JsonProperty("provider")
    private String provider;
    
    @JsonProperty("timestamp")
    private Long timestamp;
    
    @JsonProperty("request_id")
    private String requestId;

    /**
     * 创建通过的结果
     */
    public static ContentReviewResult passed() {
        ContentReviewResult result = new ContentReviewResult();
        result.setBlocked(false);
        result.setReason("Content passed review");
        result.setConfidence(1.0);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 创建被阻止的结果
     */
    public static ContentReviewResult blocked(String reason) {
        ContentReviewResult result = new ContentReviewResult();
        result.setBlocked(true);
        result.setReason(reason);
        result.setConfidence(1.0);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 创建被阻止的结果（带置信度）
     */
    public static ContentReviewResult blocked(String reason, double confidence) {
        ContentReviewResult result = new ContentReviewResult();
        result.setBlocked(true);
        result.setReason(reason);
        result.setConfidence(confidence);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 创建被阻止的结果（带分类）
     */
    public static ContentReviewResult blocked(String reason, List<String> categories) {
        ContentReviewResult result = new ContentReviewResult();
        result.setBlocked(true);
        result.setReason(reason);
        result.setCategories(categories);
        result.setConfidence(1.0);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }

    /**
     * 创建被阻止的结果（完整信息）
     */
    public static ContentReviewResult blocked(String reason, double confidence, 
                                            List<String> categories, Map<String, Object> details) {
        ContentReviewResult result = new ContentReviewResult();
        result.setBlocked(true);
        result.setReason(reason);
        result.setConfidence(confidence);
        result.setCategories(categories);
        result.setDetails(details);
        result.setTimestamp(System.currentTimeMillis());
        return result;
    }
}
