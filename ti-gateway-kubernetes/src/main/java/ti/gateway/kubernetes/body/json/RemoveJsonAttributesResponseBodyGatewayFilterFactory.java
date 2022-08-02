package ti.gateway.kubernetes.body.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class RemoveJsonAttributesResponseBodyGatewayFilterFactory extends AbstractGatewayFilterFactory<RemoveJsonAttributesResponseBodyGatewayFilterFactory.FieldListConfiguration> {
    private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory;

    public RemoveJsonAttributesResponseBodyGatewayFilterFactory(ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory) {
        this.modifyResponseBodyGatewayFilterFactory = modifyResponseBodyGatewayFilterFactory;
    }

    @Override
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST_TAIL_FLAG;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("fieldList", "deleteRecursively");
    }

    @Override
    public FieldListConfiguration newConfig() {
        return new FieldListConfiguration();
    }

    @Override
    public Class<FieldListConfiguration> getConfigClass() {
        return FieldListConfiguration.class;
    }

    private void removeJsonAttribute(JsonNode jsonBodyContent, List<String> fieldsToRemove, boolean deleteRecursively) {
        if (deleteRecursively) {
            Iterator<JsonNode> iterator = jsonBodyContent.iterator();

            label33:
            while (true) {
                JsonNode jsonNode;
                do {
                    if (! iterator.hasNext()) {
                        break label33;
                    }

                    jsonNode = iterator.next();
                    if (jsonNode instanceof ObjectNode) {
                        ((ObjectNode) jsonNode).remove(fieldsToRemove);
                        this.removeJsonAttribute(jsonNode, fieldsToRemove, true);
                    }
                } while (! (jsonNode instanceof ArrayNode));

                Iterator<JsonNode> iteratorNode = jsonNode.iterator();

                while (iteratorNode.hasNext()) {
                    JsonNode node = iteratorNode.next();
                    this.removeJsonAttribute(node, fieldsToRemove, true);
                }
            }
        }

        if (jsonBodyContent instanceof ObjectNode) {
            ((ObjectNode) jsonBodyContent).remove(fieldsToRemove);
        }
    }

    @Override
    public GatewayFilter apply(FieldListConfiguration config) {
        ModifyResponseBodyGatewayFilterFactory.Config modifyResponseBodyConfig = new ModifyResponseBodyGatewayFilterFactory.Config();
        modifyResponseBodyConfig.setInClass(String.class);
        modifyResponseBodyConfig.setOutClass(String.class);
        RewriteFunction<String, String> rewriteFunction = (exchange, body) -> {
            if (MediaType.APPLICATION_JSON.isCompatibleWith(exchange.getResponse().getHeaders().getContentType())) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonBodyContent = (JsonNode) mapper.readValue(body, JsonNode.class);
                    this.removeJsonAttribute(jsonBodyContent, config.getFieldList(), config.isDeleteRecursively());
                    body = mapper.writeValueAsString(jsonBodyContent);
                } catch (JsonProcessingException exception) {
                    throw new RuntimeException(exception);
                }
            }

            return Mono.just(body);
        };

        modifyResponseBodyConfig.setRewriteFunction(rewriteFunction);

        return this.modifyResponseBodyGatewayFilterFactory.apply(modifyResponseBodyConfig);
    }

    public static class FieldListConfiguration {
        private List<String> fieldList;
        private boolean deleteRecursively;

        public FieldListConfiguration() {
        }

        public List<String> getFieldList() {
            return fieldList;
        }

        public void setFieldList(List<String> fieldList) {
            this.fieldList = fieldList;
        }

        public boolean isDeleteRecursively() {
            return deleteRecursively;
        }

        public void setDeleteRecursively(boolean deleteRecursively) {
            this.deleteRecursively = deleteRecursively;
        }
    }

}
