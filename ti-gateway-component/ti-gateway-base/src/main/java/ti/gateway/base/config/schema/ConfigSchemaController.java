package ti.gateway.base.config.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ti.gateway.base.storage.configmap.ConfigMapSchemaStorage;

import java.util.HashMap;
import java.util.Map;

/**
 * 配置Schema管理控制器
 * 提供配置验证、转换和管理的REST API
 */
@RestController
@RequestMapping("/api/config/schema")
public class ConfigSchemaController {

    private static final Logger log = LoggerFactory.getLogger(ConfigSchemaController.class);

    @Autowired
    private ConfigMapSchemaStorage schemaStorage;

    @Autowired
    private ConfigSchemaValidator schemaValidator;

    @Autowired
    private ConfigTransformer configTransformer;

    /**
     * 获取Schema信息
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSchemaInfo() {
        try {
            ConfigSchemaValidator.SchemaInfo schemaInfo = schemaValidator.getSchemaInfo();
            
            Map<String, Object> response = new HashMap<>();
            response.put("title", schemaInfo.getTitle());
            response.put("version", schemaInfo.getVersion());
            response.put("id", schemaInfo.getId());
            response.put("description", schemaInfo.getDescription());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get schema info", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 验证YAML配置
     */
    @PostMapping("/validate/yaml")
    public ResponseEntity<Map<String, Object>> validateYaml(@RequestBody Map<String, String> request) {
        try {
            String yamlContent = request.get("content");
            if (yamlContent == null || yamlContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "YAML content is required"));
            }

            ConfigSchemaValidator.ValidationResult result = schemaValidator.validateYaml(yamlContent);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", result.isValid());
            response.put("timestamp", System.currentTimeMillis());
            
            if (!result.isValid()) {
                response.put("error", result.getErrorMessage());
                response.put("validationErrors", result.getValidationErrors());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to validate YAML", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 验证JSON配置
     */
    @PostMapping("/validate/json")
    public ResponseEntity<Map<String, Object>> validateJson(@RequestBody Map<String, String> request) {
        try {
            String jsonContent = request.get("content");
            if (jsonContent == null || jsonContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "JSON content is required"));
            }

            ConfigSchemaValidator.ValidationResult result = schemaValidator.validateJson(jsonContent);
            
            Map<String, Object> response = new HashMap<>();
            response.put("valid", result.isValid());
            response.put("timestamp", System.currentTimeMillis());
            
            if (!result.isValid()) {
                response.put("error", result.getErrorMessage());
                response.put("validationErrors", result.getValidationErrors());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to validate JSON", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 从YAML加载配置
     */
    @PostMapping("/load/yaml")
    public ResponseEntity<Map<String, Object>> loadFromYaml(@RequestBody Map<String, String> request) {
        try {
            String yamlContent = request.get("content");
            if (yamlContent == null || yamlContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "YAML content is required"));
            }

            int loadedCount = schemaStorage.loadFromYaml(yamlContent);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("loadedCount", loadedCount);
            response.put("message", "Successfully loaded " + loadedCount + " applications from YAML");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to load from YAML", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 从JSON加载配置
     */
    @PostMapping("/load/json")
    public ResponseEntity<Map<String, Object>> loadFromJson(@RequestBody Map<String, String> request) {
        try {
            String jsonContent = request.get("content");
            if (jsonContent == null || jsonContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "JSON content is required"));
            }

            int loadedCount = schemaStorage.loadFromJson(jsonContent);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("loadedCount", loadedCount);
            response.put("message", "Successfully loaded " + loadedCount + " applications from JSON");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to load from JSON", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 导出配置为YAML
     */
    @GetMapping("/export/yaml")
    public ResponseEntity<Map<String, Object>> exportToYaml() {
        try {
            String yamlContent = schemaStorage.exportToYaml();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("content", yamlContent);
            response.put("applicationCount", schemaStorage.getApplicationCount());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to export to YAML", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 获取配置统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("applicationCount", schemaStorage.getApplicationCount());
            response.put("isEmpty", schemaStorage.isEmpty());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get stats", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 刷新配置缓存
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshCache() {
        try {
            schemaStorage.refreshCache();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Configuration cache refreshed successfully");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to refresh cache", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 清空所有配置
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAll() {
        try {
            int clearedCount = schemaStorage.clearAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("clearedCount", clearedCount);
            response.put("message", "Successfully cleared " + clearedCount + " applications");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to clear configuration", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "UP");
            response.put("service", "config-schema");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Health check failed", e);
            return ResponseEntity.internalServerError().body(Map.of("status", "DOWN", "error", e.getMessage()));
        }
    }
}
