package ti.gateway.operator.springcloudgateway.apis;

import com.google.gson.reflect.TypeToken;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.ApiResponse;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.Pair;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1Scale;
import io.kubernetes.client.openapi.models.V1Status;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGateway;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayList;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayMapping;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayMappingList;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayRouteConfig;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayRouteConfigList;

public class TanzuVmwareComV1Api {
  private ApiClient localVarApiClient;

  public TanzuVmwareComV1Api() {
    this(Configuration.getDefaultApiClient());
  }

  public TanzuVmwareComV1Api(ApiClient apiClient) {
    this.localVarApiClient = apiClient;
  }

  public ApiClient getApiClient() {
    return this.localVarApiClient;
  }

  public void setApiClient(ApiClient apiClient) {
    this.localVarApiClient = apiClient;
  }

  public Call createNamespacedSpringCloudGatewayCall(
      String namespace,
      V1SpringCloudGateway body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgateways"
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (dryRun != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("dryRun", dryRun));
    }

    if (fieldManager != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldManager", fieldManager));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[] {"application/json", "application/yaml"};
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "POST",
        localVarQueryParams,
        localVarCollectionQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call createNamespacedSpringCloudGatewayValidateBeforeCall(
      String namespace,
      V1SpringCloudGateway body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " createNamespacedSpringCloudGateway(Async)");
    } else if (body == null) {
      throw new ApiException(
          "Missing the required parameter 'body' when calling"
              + " createNamespacedSpringCloudGateway(Async)");
    } else {
      Call localVarCall =
          this.createNamespacedSpringCloudGatewayCall(
              namespace, body, pretty, dryRun, fieldManager, _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGateway createNamespacedSpringCloudGateway(
      String namespace,
      V1SpringCloudGateway body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    ApiResponse<V1SpringCloudGateway> localVarResp =
        this.createNamespacedSpringCloudGatewayWithHttpInfo(
            namespace, body, pretty, dryRun, fieldManager);
    return (V1SpringCloudGateway) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGateway> createNamespacedSpringCloudGatewayWithHttpInfo(
      String namespace,
      V1SpringCloudGateway body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    Call localVarCall =
        this.createNamespacedSpringCloudGatewayValidateBeforeCall(
            namespace, body, pretty, dryRun, fieldManager, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGateway>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call createNamespacedSpringCloudGatewayAsync(
      String namespace,
      V1SpringCloudGateway body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback<V1SpringCloudGateway> _callback)
      throws ApiException {
    Call localVarCall =
        this.createNamespacedSpringCloudGatewayValidateBeforeCall(
            namespace, body, pretty, dryRun, fieldManager, _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGateway>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call createNamespacedSpringCloudGatewayMappingCall(
      String namespace,
      V1SpringCloudGatewayMapping body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgatewaymappings"
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (dryRun != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("dryRun", dryRun));
    }

    if (fieldManager != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldManager", fieldManager));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[] {"application/json", "application/yaml"};
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "POST",
        localVarQueryParams,
        localVarCollectionQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call createNamespacedSpringCloudGatewayMappingValidateBeforeCall(
      String namespace,
      V1SpringCloudGatewayMapping body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " createNamespacedSpringCloudGatewayMapping(Async)");
    } else if (body == null) {
      throw new ApiException(
          "Missing the required parameter 'body' when calling"
              + " createNamespacedSpringCloudGatewayMapping(Async)");
    } else {
      Call localVarCall =
          this.createNamespacedSpringCloudGatewayMappingCall(
              namespace, body, pretty, dryRun, fieldManager, _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGatewayMapping createNamespacedSpringCloudGatewayMapping(
      String namespace,
      V1SpringCloudGatewayMapping body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    ApiResponse<V1SpringCloudGatewayMapping> localVarResp =
        this.createNamespacedSpringCloudGatewayMappingWithHttpInfo(
            namespace, body, pretty, dryRun, fieldManager);
    return (V1SpringCloudGatewayMapping) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGatewayMapping>
      createNamespacedSpringCloudGatewayMappingWithHttpInfo(
          String namespace,
          V1SpringCloudGatewayMapping body,
          String pretty,
          String dryRun,
          String fieldManager)
          throws ApiException {
    Call localVarCall =
        this.createNamespacedSpringCloudGatewayMappingValidateBeforeCall(
            namespace, body, pretty, dryRun, fieldManager, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayMapping>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call createNamespacedSpringCloudGatewayMappingAsync(
      String namespace,
      V1SpringCloudGatewayMapping body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback<V1SpringCloudGatewayMapping> _callback)
      throws ApiException {
    Call localVarCall =
        this.createNamespacedSpringCloudGatewayMappingValidateBeforeCall(
            namespace, body, pretty, dryRun, fieldManager, _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayMapping>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call createNamespacedSpringCloudGatewayRouteConfigCall(
      String namespace,
      V1SpringCloudGatewayRouteConfig body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgatewayrouteconfigs"
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (dryRun != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("dryRun", dryRun));
    }

    if (fieldManager != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldManager", fieldManager));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[] {"application/json", "application/yaml"};
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "POST",
        localVarQueryParams,
        localVarCollectionQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call createNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
      String namespace,
      V1SpringCloudGatewayRouteConfig body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " createNamespacedSpringCloudGatewayRouteConfig(Async)");
    } else if (body == null) {
      throw new ApiException(
          "Missing the required parameter 'body' when calling"
              + " createNamespacedSpringCloudGatewayRouteConfig(Async)");
    } else {
      Call localVarCall =
          this.createNamespacedSpringCloudGatewayRouteConfigCall(
              namespace, body, pretty, dryRun, fieldManager, _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGatewayRouteConfig createNamespacedSpringCloudGatewayRouteConfig(
      String namespace,
      V1SpringCloudGatewayRouteConfig body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    ApiResponse<V1SpringCloudGatewayRouteConfig> localVarResp =
        this.createNamespacedSpringCloudGatewayRouteConfigWithHttpInfo(
            namespace, body, pretty, dryRun, fieldManager);
    return (V1SpringCloudGatewayRouteConfig) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGatewayRouteConfig>
      createNamespacedSpringCloudGatewayRouteConfigWithHttpInfo(
          String namespace,
          V1SpringCloudGatewayRouteConfig body,
          String pretty,
          String dryRun,
          String fieldManager)
          throws ApiException {
    Call localVarCall =
        this.createNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
            namespace, body, pretty, dryRun, fieldManager, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayRouteConfig>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call createNamespacedSpringCloudGatewayRouteConfigAsync(
      String namespace,
      V1SpringCloudGatewayRouteConfig body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback<V1SpringCloudGatewayRouteConfig> _callback)
      throws ApiException {
    Call localVarCall =
        this.createNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
            namespace, body, pretty, dryRun, fieldManager, _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayRouteConfig>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call deleteCollectionNamespacedSpringCloudGatewayCall(
      String namespace,
      String pretty,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      final ApiCallback _callback)
      throws ApiException {
    Object localVarPostBody = null;
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgateways"
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (_continue != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("continue", _continue));
    }

    if (fieldSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldSelector", fieldSelector));
    }

    if (labelSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("labelSelector", labelSelector));
    }

    if (limit != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("limit", limit));
    }

    if (resourceVersion != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersion", resourceVersion));
    }

    if (resourceVersionMatch != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersionMatch", resourceVersionMatch));
    }

    if (timeoutSeconds != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("timeoutSeconds", timeoutSeconds));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[0];
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "DELETE",
        localVarQueryParams,
        localVarCollectionQueryParams,
        localVarPostBody,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call deleteCollectionNamespacedSpringCloudGatewayValidateBeforeCall(
      String namespace,
      String pretty,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      final ApiCallback _callback)
      throws ApiException {
    if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " deleteCollectionNamespacedSpringCloudGateway(Async)");
    } else {
      Call localVarCall =
          this.deleteCollectionNamespacedSpringCloudGatewayCall(
              namespace,
              pretty,
              _continue,
              fieldSelector,
              labelSelector,
              limit,
              resourceVersion,
              resourceVersionMatch,
              timeoutSeconds,
              _callback);
      return localVarCall;
    }
  }

  public V1Status deleteCollectionNamespacedSpringCloudGateway(
      String namespace,
      String pretty,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds)
      throws ApiException {
    ApiResponse<V1Status> localVarResp =
        this.deleteCollectionNamespacedSpringCloudGatewayWithHttpInfo(
            namespace,
            pretty,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds);
    return (V1Status) localVarResp.getData();
  }

  public ApiResponse<V1Status> deleteCollectionNamespacedSpringCloudGatewayWithHttpInfo(
      String namespace,
      String pretty,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds)
      throws ApiException {
    Call localVarCall =
        this.deleteCollectionNamespacedSpringCloudGatewayValidateBeforeCall(
            namespace,
            pretty,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1Status>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call deleteCollectionNamespacedSpringCloudGatewayAsync(
      String namespace,
      String pretty,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      final ApiCallback<V1Status> _callback)
      throws ApiException {
    Call localVarCall =
        this.deleteCollectionNamespacedSpringCloudGatewayValidateBeforeCall(
            namespace,
            pretty,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            _callback);
    Type localVarReturnType = (new TypeToken<V1Status>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call deleteCollectionNamespacedSpringCloudGatewayMappingCall(
      String namespace,
      String pretty,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      final ApiCallback _callback)
      throws ApiException {
    Object localVarPostBody = null;
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgatewaymappings"
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (_continue != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("continue", _continue));
    }

    if (fieldSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldSelector", fieldSelector));
    }

    if (labelSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("labelSelector", labelSelector));
    }

    if (limit != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("limit", limit));
    }

    if (resourceVersion != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersion", resourceVersion));
    }

    if (resourceVersionMatch != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersionMatch", resourceVersionMatch));
    }

    if (timeoutSeconds != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("timeoutSeconds", timeoutSeconds));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[0];
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "DELETE",
        localVarQueryParams,
        localVarCollectionQueryParams,
        localVarPostBody,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call deleteCollectionNamespacedSpringCloudGatewayMappingValidateBeforeCall(
      String namespace,
      String pretty,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      final ApiCallback _callback)
      throws ApiException {
    if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " deleteCollectionNamespacedSpringCloudGatewayMapping(Async)");
    } else {
      Call localVarCall =
          this.deleteCollectionNamespacedSpringCloudGatewayMappingCall(
              namespace,
              pretty,
              _continue,
              fieldSelector,
              labelSelector,
              limit,
              resourceVersion,
              resourceVersionMatch,
              timeoutSeconds,
              _callback);
      return localVarCall;
    }
  }

  public V1Status deleteCollectionNamespacedSpringCloudGatewayMapping(
      String namespace,
      String pretty,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds)
      throws ApiException {
    ApiResponse<V1Status> localVarResp =
        this.deleteCollectionNamespacedSpringCloudGatewayMappingWithHttpInfo(
            namespace,
            pretty,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds);
    return (V1Status) localVarResp.getData();
  }

  public ApiResponse<V1Status> deleteCollectionNamespacedSpringCloudGatewayMappingWithHttpInfo(
      String namespace,
      String pretty,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds)
      throws ApiException {
    Call localVarCall =
        this.deleteCollectionNamespacedSpringCloudGatewayMappingValidateBeforeCall(
            namespace,
            pretty,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1Status>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call deleteCollectionNamespacedSpringCloudGatewayMappingAsync(
      String namespace,
      String pretty,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      final ApiCallback<V1Status> _callback)
      throws ApiException {
    Call localVarCall =
        this.deleteCollectionNamespacedSpringCloudGatewayMappingValidateBeforeCall(
            namespace,
            pretty,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            _callback);
    Type localVarReturnType = (new TypeToken<V1Status>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call deleteCollectionNamespacedSpringCloudGatewayRouteConfigCall(
      String namespace,
      String pretty,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      final ApiCallback _callback)
      throws ApiException {
    Object localVarPostBody = null;
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgatewayrouteconfigs"
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (_continue != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("continue", _continue));
    }

    if (fieldSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldSelector", fieldSelector));
    }

    if (labelSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("labelSelector", labelSelector));
    }

    if (limit != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("limit", limit));
    }

    if (resourceVersion != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersion", resourceVersion));
    }

    if (resourceVersionMatch != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersionMatch", resourceVersionMatch));
    }

    if (timeoutSeconds != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("timeoutSeconds", timeoutSeconds));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[0];
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "DELETE",
        localVarQueryParams,
        localVarCollectionQueryParams,
        localVarPostBody,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call deleteCollectionNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
      String namespace,
      String pretty,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      final ApiCallback _callback)
      throws ApiException {
    if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " deleteCollectionNamespacedSpringCloudGatewayRouteConfig(Async)");
    } else {
      Call localVarCall =
          this.deleteCollectionNamespacedSpringCloudGatewayRouteConfigCall(
              namespace,
              pretty,
              _continue,
              fieldSelector,
              labelSelector,
              limit,
              resourceVersion,
              resourceVersionMatch,
              timeoutSeconds,
              _callback);
      return localVarCall;
    }
  }

  public V1Status deleteCollectionNamespacedSpringCloudGatewayRouteConfig(
      String namespace,
      String pretty,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds)
      throws ApiException {
    ApiResponse<V1Status> localVarResp =
        this.deleteCollectionNamespacedSpringCloudGatewayRouteConfigWithHttpInfo(
            namespace,
            pretty,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds);
    return (V1Status) localVarResp.getData();
  }

  public ApiResponse<V1Status> deleteCollectionNamespacedSpringCloudGatewayRouteConfigWithHttpInfo(
      String namespace,
      String pretty,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds)
      throws ApiException {
    Call localVarCall =
        this.deleteCollectionNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
            namespace,
            pretty,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1Status>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call deleteCollectionNamespacedSpringCloudGatewayRouteConfigAsync(
      String namespace,
      String pretty,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      final ApiCallback<V1Status> _callback)
      throws ApiException {
    Call localVarCall =
        this.deleteCollectionNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
            namespace,
            pretty,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            _callback);
    Type localVarReturnType = (new TypeToken<V1Status>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call deleteNamespacedSpringCloudGatewayCall(
      String name,
      String namespace,
      String pretty,
      String dryRun,
      Integer gracePeriodSeconds,
      Boolean orphanDependents,
      String propagationPolicy,
      V1DeleteOptions body,
      final ApiCallback _callback)
      throws ApiException {
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgateways/{name}"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (dryRun != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("dryRun", dryRun));
    }

    if (gracePeriodSeconds != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("gracePeriodSeconds", gracePeriodSeconds));
    }

    if (orphanDependents != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("orphanDependents", orphanDependents));
    }

    if (propagationPolicy != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("propagationPolicy", propagationPolicy));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[] {"application/json", "application/yaml"};
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "DELETE",
        localVarQueryParams,
        localVarCollectionQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call deleteNamespacedSpringCloudGatewayValidateBeforeCall(
      String name,
      String namespace,
      String pretty,
      String dryRun,
      Integer gracePeriodSeconds,
      Boolean orphanDependents,
      String propagationPolicy,
      V1DeleteOptions body,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " deleteNamespacedSpringCloudGateway(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " deleteNamespacedSpringCloudGateway(Async)");
    } else {
      Call localVarCall =
          this.deleteNamespacedSpringCloudGatewayCall(
              name,
              namespace,
              pretty,
              dryRun,
              gracePeriodSeconds,
              orphanDependents,
              propagationPolicy,
              body,
              _callback);
      return localVarCall;
    }
  }

  public V1Status deleteNamespacedSpringCloudGateway(
      String name,
      String namespace,
      String pretty,
      String dryRun,
      Integer gracePeriodSeconds,
      Boolean orphanDependents,
      String propagationPolicy,
      V1DeleteOptions body)
      throws ApiException {
    ApiResponse<V1Status> localVarResp =
        this.deleteNamespacedSpringCloudGatewayWithHttpInfo(
            name,
            namespace,
            pretty,
            dryRun,
            gracePeriodSeconds,
            orphanDependents,
            propagationPolicy,
            body);
    return (V1Status) localVarResp.getData();
  }

  public ApiResponse<V1Status> deleteNamespacedSpringCloudGatewayWithHttpInfo(
      String name,
      String namespace,
      String pretty,
      String dryRun,
      Integer gracePeriodSeconds,
      Boolean orphanDependents,
      String propagationPolicy,
      V1DeleteOptions body)
      throws ApiException {
    Call localVarCall =
        this.deleteNamespacedSpringCloudGatewayValidateBeforeCall(
            name,
            namespace,
            pretty,
            dryRun,
            gracePeriodSeconds,
            orphanDependents,
            propagationPolicy,
            body,
            (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1Status>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call deleteNamespacedSpringCloudGatewayAsync(
      String name,
      String namespace,
      String pretty,
      String dryRun,
      Integer gracePeriodSeconds,
      Boolean orphanDependents,
      String propagationPolicy,
      V1DeleteOptions body,
      final ApiCallback<V1Status> _callback)
      throws ApiException {
    Call localVarCall =
        this.deleteNamespacedSpringCloudGatewayValidateBeforeCall(
            name,
            namespace,
            pretty,
            dryRun,
            gracePeriodSeconds,
            orphanDependents,
            propagationPolicy,
            body,
            _callback);
    Type localVarReturnType = (new TypeToken<V1Status>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call deleteNamespacedSpringCloudGatewayMappingCall(
      String name,
      String namespace,
      String pretty,
      String dryRun,
      Integer gracePeriodSeconds,
      Boolean orphanDependents,
      String propagationPolicy,
      V1DeleteOptions body,
      final ApiCallback _callback)
      throws ApiException {
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgatewaymappings/{name}"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (dryRun != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("dryRun", dryRun));
    }

    if (gracePeriodSeconds != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("gracePeriodSeconds", gracePeriodSeconds));
    }

    if (orphanDependents != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("orphanDependents", orphanDependents));
    }

    if (propagationPolicy != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("propagationPolicy", propagationPolicy));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[] {"application/json", "application/yaml"};
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "DELETE",
        localVarQueryParams,
        localVarCollectionQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call deleteNamespacedSpringCloudGatewayMappingValidateBeforeCall(
      String name,
      String namespace,
      String pretty,
      String dryRun,
      Integer gracePeriodSeconds,
      Boolean orphanDependents,
      String propagationPolicy,
      V1DeleteOptions body,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " deleteNamespacedSpringCloudGatewayMapping(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " deleteNamespacedSpringCloudGatewayMapping(Async)");
    } else {
      Call localVarCall =
          this.deleteNamespacedSpringCloudGatewayMappingCall(
              name,
              namespace,
              pretty,
              dryRun,
              gracePeriodSeconds,
              orphanDependents,
              propagationPolicy,
              body,
              _callback);
      return localVarCall;
    }
  }

  public V1Status deleteNamespacedSpringCloudGatewayMapping(
      String name,
      String namespace,
      String pretty,
      String dryRun,
      Integer gracePeriodSeconds,
      Boolean orphanDependents,
      String propagationPolicy,
      V1DeleteOptions body)
      throws ApiException {
    ApiResponse<V1Status> localVarResp =
        this.deleteNamespacedSpringCloudGatewayMappingWithHttpInfo(
            name,
            namespace,
            pretty,
            dryRun,
            gracePeriodSeconds,
            orphanDependents,
            propagationPolicy,
            body);
    return (V1Status) localVarResp.getData();
  }

  public ApiResponse<V1Status> deleteNamespacedSpringCloudGatewayMappingWithHttpInfo(
      String name,
      String namespace,
      String pretty,
      String dryRun,
      Integer gracePeriodSeconds,
      Boolean orphanDependents,
      String propagationPolicy,
      V1DeleteOptions body)
      throws ApiException {
    Call localVarCall =
        this.deleteNamespacedSpringCloudGatewayMappingValidateBeforeCall(
            name,
            namespace,
            pretty,
            dryRun,
            gracePeriodSeconds,
            orphanDependents,
            propagationPolicy,
            body,
            (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1Status>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call deleteNamespacedSpringCloudGatewayMappingAsync(
      String name,
      String namespace,
      String pretty,
      String dryRun,
      Integer gracePeriodSeconds,
      Boolean orphanDependents,
      String propagationPolicy,
      V1DeleteOptions body,
      final ApiCallback<V1Status> _callback)
      throws ApiException {
    Call localVarCall =
        this.deleteNamespacedSpringCloudGatewayMappingValidateBeforeCall(
            name,
            namespace,
            pretty,
            dryRun,
            gracePeriodSeconds,
            orphanDependents,
            propagationPolicy,
            body,
            _callback);
    Type localVarReturnType = (new TypeToken<V1Status>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call deleteNamespacedSpringCloudGatewayRouteConfigCall(
      String name,
      String namespace,
      String pretty,
      String dryRun,
      Integer gracePeriodSeconds,
      Boolean orphanDependents,
      String propagationPolicy,
      V1DeleteOptions body,
      final ApiCallback _callback)
      throws ApiException {
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgatewayrouteconfigs/{name}"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (dryRun != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("dryRun", dryRun));
    }

    if (gracePeriodSeconds != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("gracePeriodSeconds", gracePeriodSeconds));
    }

    if (orphanDependents != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("orphanDependents", orphanDependents));
    }

    if (propagationPolicy != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("propagationPolicy", propagationPolicy));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[] {"application/json", "application/yaml"};
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "DELETE",
        localVarQueryParams,
        localVarCollectionQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call deleteNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
      String name,
      String namespace,
      String pretty,
      String dryRun,
      Integer gracePeriodSeconds,
      Boolean orphanDependents,
      String propagationPolicy,
      V1DeleteOptions body,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " deleteNamespacedSpringCloudGatewayRouteConfig(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " deleteNamespacedSpringCloudGatewayRouteConfig(Async)");
    } else {
      Call localVarCall =
          this.deleteNamespacedSpringCloudGatewayRouteConfigCall(
              name,
              namespace,
              pretty,
              dryRun,
              gracePeriodSeconds,
              orphanDependents,
              propagationPolicy,
              body,
              _callback);
      return localVarCall;
    }
  }

  public V1Status deleteNamespacedSpringCloudGatewayRouteConfig(
      String name,
      String namespace,
      String pretty,
      String dryRun,
      Integer gracePeriodSeconds,
      Boolean orphanDependents,
      String propagationPolicy,
      V1DeleteOptions body)
      throws ApiException {
    ApiResponse<V1Status> localVarResp =
        this.deleteNamespacedSpringCloudGatewayRouteConfigWithHttpInfo(
            name,
            namespace,
            pretty,
            dryRun,
            gracePeriodSeconds,
            orphanDependents,
            propagationPolicy,
            body);
    return (V1Status) localVarResp.getData();
  }

  public ApiResponse<V1Status> deleteNamespacedSpringCloudGatewayRouteConfigWithHttpInfo(
      String name,
      String namespace,
      String pretty,
      String dryRun,
      Integer gracePeriodSeconds,
      Boolean orphanDependents,
      String propagationPolicy,
      V1DeleteOptions body)
      throws ApiException {
    Call localVarCall =
        this.deleteNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
            name,
            namespace,
            pretty,
            dryRun,
            gracePeriodSeconds,
            orphanDependents,
            propagationPolicy,
            body,
            (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1Status>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call deleteNamespacedSpringCloudGatewayRouteConfigAsync(
      String name,
      String namespace,
      String pretty,
      String dryRun,
      Integer gracePeriodSeconds,
      Boolean orphanDependents,
      String propagationPolicy,
      V1DeleteOptions body,
      final ApiCallback<V1Status> _callback)
      throws ApiException {
    Call localVarCall =
        this.deleteNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
            name,
            namespace,
            pretty,
            dryRun,
            gracePeriodSeconds,
            orphanDependents,
            propagationPolicy,
            body,
            _callback);
    Type localVarReturnType = (new TypeToken<V1Status>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call listNamespacedSpringCloudGatewayCall(
      String namespace,
      String pretty,
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback _callback)
      throws ApiException {
    Object localVarPostBody = null;
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgateways"
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (allowWatchBookmarks != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("allowWatchBookmarks", allowWatchBookmarks));
    }

    if (_continue != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("continue", _continue));
    }

    if (fieldSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldSelector", fieldSelector));
    }

    if (labelSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("labelSelector", labelSelector));
    }

    if (limit != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("limit", limit));
    }

    if (resourceVersion != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersion", resourceVersion));
    }

    if (resourceVersionMatch != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersionMatch", resourceVersionMatch));
    }

    if (timeoutSeconds != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("timeoutSeconds", timeoutSeconds));
    }

    if (watch != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("watch", watch));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[0];
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "GET",
        localVarQueryParams,
        localVarCollectionQueryParams,
        localVarPostBody,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call listNamespacedSpringCloudGatewayValidateBeforeCall(
      String namespace,
      String pretty,
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback _callback)
      throws ApiException {
    if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " listNamespacedSpringCloudGateway(Async)");
    } else {
      Call localVarCall =
          this.listNamespacedSpringCloudGatewayCall(
              namespace,
              pretty,
              allowWatchBookmarks,
              _continue,
              fieldSelector,
              labelSelector,
              limit,
              resourceVersion,
              resourceVersionMatch,
              timeoutSeconds,
              watch,
              _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGatewayList listNamespacedSpringCloudGateway(
      String namespace,
      String pretty,
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch)
      throws ApiException {
    ApiResponse<V1SpringCloudGatewayList> localVarResp =
        this.listNamespacedSpringCloudGatewayWithHttpInfo(
            namespace,
            pretty,
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch);
    return (V1SpringCloudGatewayList) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGatewayList> listNamespacedSpringCloudGatewayWithHttpInfo(
      String namespace,
      String pretty,
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch)
      throws ApiException {
    Call localVarCall =
        this.listNamespacedSpringCloudGatewayValidateBeforeCall(
            namespace,
            pretty,
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch,
            (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayList>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call listNamespacedSpringCloudGatewayAsync(
      String namespace,
      String pretty,
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback<V1SpringCloudGatewayList> _callback)
      throws ApiException {
    Call localVarCall =
        this.listNamespacedSpringCloudGatewayValidateBeforeCall(
            namespace,
            pretty,
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch,
            _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayList>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call listNamespacedSpringCloudGatewayMappingCall(
      String namespace,
      String pretty,
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback _callback)
      throws ApiException {
    Object localVarPostBody = null;
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgatewaymappings"
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (allowWatchBookmarks != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("allowWatchBookmarks", allowWatchBookmarks));
    }

    if (_continue != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("continue", _continue));
    }

    if (fieldSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldSelector", fieldSelector));
    }

    if (labelSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("labelSelector", labelSelector));
    }

    if (limit != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("limit", limit));
    }

    if (resourceVersion != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersion", resourceVersion));
    }

    if (resourceVersionMatch != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersionMatch", resourceVersionMatch));
    }

    if (timeoutSeconds != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("timeoutSeconds", timeoutSeconds));
    }

    if (watch != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("watch", watch));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[0];
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "GET",
        localVarQueryParams,
        localVarCollectionQueryParams,
        localVarPostBody,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call listNamespacedSpringCloudGatewayMappingValidateBeforeCall(
      String namespace,
      String pretty,
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback _callback)
      throws ApiException {
    if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " listNamespacedSpringCloudGatewayMapping(Async)");
    } else {
      Call localVarCall =
          this.listNamespacedSpringCloudGatewayMappingCall(
              namespace,
              pretty,
              allowWatchBookmarks,
              _continue,
              fieldSelector,
              labelSelector,
              limit,
              resourceVersion,
              resourceVersionMatch,
              timeoutSeconds,
              watch,
              _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGatewayMappingList listNamespacedSpringCloudGatewayMapping(
      String namespace,
      String pretty,
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch)
      throws ApiException {
    ApiResponse<V1SpringCloudGatewayMappingList> localVarResp =
        this.listNamespacedSpringCloudGatewayMappingWithHttpInfo(
            namespace,
            pretty,
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch);
    return (V1SpringCloudGatewayMappingList) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGatewayMappingList>
      listNamespacedSpringCloudGatewayMappingWithHttpInfo(
          String namespace,
          String pretty,
          Boolean allowWatchBookmarks,
          String _continue,
          String fieldSelector,
          String labelSelector,
          Integer limit,
          String resourceVersion,
          String resourceVersionMatch,
          Integer timeoutSeconds,
          Boolean watch)
          throws ApiException {
    Call localVarCall =
        this.listNamespacedSpringCloudGatewayMappingValidateBeforeCall(
            namespace,
            pretty,
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch,
            (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayMappingList>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call listNamespacedSpringCloudGatewayMappingAsync(
      String namespace,
      String pretty,
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback<V1SpringCloudGatewayMappingList> _callback)
      throws ApiException {
    Call localVarCall =
        this.listNamespacedSpringCloudGatewayMappingValidateBeforeCall(
            namespace,
            pretty,
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch,
            _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayMappingList>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call listNamespacedSpringCloudGatewayRouteConfigCall(
      String namespace,
      String pretty,
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback _callback)
      throws ApiException {
    Object localVarPostBody = null;
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgatewayrouteconfigs"
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (allowWatchBookmarks != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("allowWatchBookmarks", allowWatchBookmarks));
    }

    if (_continue != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("continue", _continue));
    }

    if (fieldSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldSelector", fieldSelector));
    }

    if (labelSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("labelSelector", labelSelector));
    }

    if (limit != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("limit", limit));
    }

    if (resourceVersion != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersion", resourceVersion));
    }

    if (resourceVersionMatch != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersionMatch", resourceVersionMatch));
    }

    if (timeoutSeconds != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("timeoutSeconds", timeoutSeconds));
    }

    if (watch != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("watch", watch));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[0];
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "GET",
        localVarQueryParams,
        localVarCollectionQueryParams,
        localVarPostBody,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call listNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
      String namespace,
      String pretty,
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback _callback)
      throws ApiException {
    if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " listNamespacedSpringCloudGatewayRouteConfig(Async)");
    } else {
      Call localVarCall =
          this.listNamespacedSpringCloudGatewayRouteConfigCall(
              namespace,
              pretty,
              allowWatchBookmarks,
              _continue,
              fieldSelector,
              labelSelector,
              limit,
              resourceVersion,
              resourceVersionMatch,
              timeoutSeconds,
              watch,
              _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGatewayRouteConfigList listNamespacedSpringCloudGatewayRouteConfig(
      String namespace,
      String pretty,
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch)
      throws ApiException {
    ApiResponse<V1SpringCloudGatewayRouteConfigList> localVarResp =
        this.listNamespacedSpringCloudGatewayRouteConfigWithHttpInfo(
            namespace,
            pretty,
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch);
    return (V1SpringCloudGatewayRouteConfigList) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGatewayRouteConfigList>
      listNamespacedSpringCloudGatewayRouteConfigWithHttpInfo(
          String namespace,
          String pretty,
          Boolean allowWatchBookmarks,
          String _continue,
          String fieldSelector,
          String labelSelector,
          Integer limit,
          String resourceVersion,
          String resourceVersionMatch,
          Integer timeoutSeconds,
          Boolean watch)
          throws ApiException {
    Call localVarCall =
        this.listNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
            namespace,
            pretty,
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch,
            (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayRouteConfigList>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call listNamespacedSpringCloudGatewayRouteConfigAsync(
      String namespace,
      String pretty,
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback<V1SpringCloudGatewayRouteConfigList> _callback)
      throws ApiException {
    Call localVarCall =
        this.listNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
            namespace,
            pretty,
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch,
            _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayRouteConfigList>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call listSpringCloudGatewayForAllNamespacesCall(
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String pretty,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback _callback)
      throws ApiException {
    Object localVarPostBody = null;
    String localVarPath = "/apis/tanzu.vmware.com/v1/springcloudgateways";
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (allowWatchBookmarks != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("allowWatchBookmarks", allowWatchBookmarks));
    }

    if (_continue != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("continue", _continue));
    }

    if (fieldSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldSelector", fieldSelector));
    }

    if (labelSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("labelSelector", labelSelector));
    }

    if (limit != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("limit", limit));
    }

    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (resourceVersion != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersion", resourceVersion));
    }

    if (resourceVersionMatch != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersionMatch", resourceVersionMatch));
    }

    if (timeoutSeconds != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("timeoutSeconds", timeoutSeconds));
    }

    if (watch != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("watch", watch));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[0];
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "GET",
        localVarQueryParams,
        localVarCollectionQueryParams,
        localVarPostBody,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call listSpringCloudGatewayForAllNamespacesValidateBeforeCall(
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String pretty,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback _callback)
      throws ApiException {
    Call localVarCall =
        this.listSpringCloudGatewayForAllNamespacesCall(
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            pretty,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch,
            _callback);
    return localVarCall;
  }

  public V1SpringCloudGatewayList listSpringCloudGatewayForAllNamespaces(
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String pretty,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch)
      throws ApiException {
    ApiResponse<V1SpringCloudGatewayList> localVarResp =
        this.listSpringCloudGatewayForAllNamespacesWithHttpInfo(
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            pretty,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch);
    return (V1SpringCloudGatewayList) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGatewayList> listSpringCloudGatewayForAllNamespacesWithHttpInfo(
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String pretty,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch)
      throws ApiException {
    Call localVarCall =
        this.listSpringCloudGatewayForAllNamespacesValidateBeforeCall(
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            pretty,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch,
            (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayList>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call listSpringCloudGatewayForAllNamespacesAsync(
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String pretty,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback<V1SpringCloudGatewayList> _callback)
      throws ApiException {
    Call localVarCall =
        this.listSpringCloudGatewayForAllNamespacesValidateBeforeCall(
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            pretty,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch,
            _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayList>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call listSpringCloudGatewayMappingForAllNamespacesCall(
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String pretty,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback _callback)
      throws ApiException {
    Object localVarPostBody = null;
    String localVarPath = "/apis/tanzu.vmware.com/v1/springcloudgatewaymappings";
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (allowWatchBookmarks != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("allowWatchBookmarks", allowWatchBookmarks));
    }

    if (_continue != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("continue", _continue));
    }

    if (fieldSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldSelector", fieldSelector));
    }

    if (labelSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("labelSelector", labelSelector));
    }

    if (limit != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("limit", limit));
    }

    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (resourceVersion != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersion", resourceVersion));
    }

    if (resourceVersionMatch != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersionMatch", resourceVersionMatch));
    }

    if (timeoutSeconds != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("timeoutSeconds", timeoutSeconds));
    }

    if (watch != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("watch", watch));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[0];
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "GET",
        localVarQueryParams,
        localVarCollectionQueryParams,
        localVarPostBody,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call listSpringCloudGatewayMappingForAllNamespacesValidateBeforeCall(
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String pretty,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback _callback)
      throws ApiException {
    Call localVarCall =
        this.listSpringCloudGatewayMappingForAllNamespacesCall(
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            pretty,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch,
            _callback);
    return localVarCall;
  }

  public V1SpringCloudGatewayMappingList listSpringCloudGatewayMappingForAllNamespaces(
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String pretty,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch)
      throws ApiException {
    ApiResponse<V1SpringCloudGatewayMappingList> localVarResp =
        this.listSpringCloudGatewayMappingForAllNamespacesWithHttpInfo(
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            pretty,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch);
    return (V1SpringCloudGatewayMappingList) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGatewayMappingList>
      listSpringCloudGatewayMappingForAllNamespacesWithHttpInfo(
          Boolean allowWatchBookmarks,
          String _continue,
          String fieldSelector,
          String labelSelector,
          Integer limit,
          String pretty,
          String resourceVersion,
          String resourceVersionMatch,
          Integer timeoutSeconds,
          Boolean watch)
          throws ApiException {
    Call localVarCall =
        this.listSpringCloudGatewayMappingForAllNamespacesValidateBeforeCall(
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            pretty,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch,
            (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayMappingList>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call listSpringCloudGatewayMappingForAllNamespacesAsync(
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String pretty,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback<V1SpringCloudGatewayMappingList> _callback)
      throws ApiException {
    Call localVarCall =
        this.listSpringCloudGatewayMappingForAllNamespacesValidateBeforeCall(
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            pretty,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch,
            _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayMappingList>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call listSpringCloudGatewayRouteConfigForAllNamespacesCall(
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String pretty,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback _callback)
      throws ApiException {
    Object localVarPostBody = null;
    String localVarPath = "/apis/tanzu.vmware.com/v1/springcloudgatewayrouteconfigs";
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (allowWatchBookmarks != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("allowWatchBookmarks", allowWatchBookmarks));
    }

    if (_continue != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("continue", _continue));
    }

    if (fieldSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldSelector", fieldSelector));
    }

    if (labelSelector != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("labelSelector", labelSelector));
    }

    if (limit != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("limit", limit));
    }

    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (resourceVersion != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersion", resourceVersion));
    }

    if (resourceVersionMatch != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersionMatch", resourceVersionMatch));
    }

    if (timeoutSeconds != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("timeoutSeconds", timeoutSeconds));
    }

    if (watch != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("watch", watch));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[0];
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "GET",
        localVarQueryParams,
        localVarCollectionQueryParams,
        localVarPostBody,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call listSpringCloudGatewayRouteConfigForAllNamespacesValidateBeforeCall(
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String pretty,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback _callback)
      throws ApiException {
    Call localVarCall =
        this.listSpringCloudGatewayRouteConfigForAllNamespacesCall(
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            pretty,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch,
            _callback);
    return localVarCall;
  }

  public V1SpringCloudGatewayRouteConfigList listSpringCloudGatewayRouteConfigForAllNamespaces(
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String pretty,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch)
      throws ApiException {
    ApiResponse<V1SpringCloudGatewayRouteConfigList> localVarResp =
        this.listSpringCloudGatewayRouteConfigForAllNamespacesWithHttpInfo(
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            pretty,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch);
    return (V1SpringCloudGatewayRouteConfigList) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGatewayRouteConfigList>
      listSpringCloudGatewayRouteConfigForAllNamespacesWithHttpInfo(
          Boolean allowWatchBookmarks,
          String _continue,
          String fieldSelector,
          String labelSelector,
          Integer limit,
          String pretty,
          String resourceVersion,
          String resourceVersionMatch,
          Integer timeoutSeconds,
          Boolean watch)
          throws ApiException {
    Call localVarCall =
        this.listSpringCloudGatewayRouteConfigForAllNamespacesValidateBeforeCall(
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            pretty,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch,
            (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayRouteConfigList>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call listSpringCloudGatewayRouteConfigForAllNamespacesAsync(
      Boolean allowWatchBookmarks,
      String _continue,
      String fieldSelector,
      String labelSelector,
      Integer limit,
      String pretty,
      String resourceVersion,
      String resourceVersionMatch,
      Integer timeoutSeconds,
      Boolean watch,
      final ApiCallback<V1SpringCloudGatewayRouteConfigList> _callback)
      throws ApiException {
    Call localVarCall =
        this.listSpringCloudGatewayRouteConfigForAllNamespacesValidateBeforeCall(
            allowWatchBookmarks,
            _continue,
            fieldSelector,
            labelSelector,
            limit,
            pretty,
            resourceVersion,
            resourceVersionMatch,
            timeoutSeconds,
            watch,
            _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayRouteConfigList>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call patchNamespacedSpringCloudGatewayCall(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgateways/{name}"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (dryRun != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("dryRun", dryRun));
    }

    if (fieldManager != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldManager", fieldManager));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes =
        new String[] {
          "application/json-patch+json",
          "application/merge-patch+json",
          "application/apply-patch+yaml"
        };
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "PATCH",
        localVarQueryParams,
        localVarCollectionQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call patchNamespacedSpringCloudGatewayValidateBeforeCall(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " patchNamespacedSpringCloudGateway(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " patchNamespacedSpringCloudGateway(Async)");
    } else if (body == null) {
      throw new ApiException(
          "Missing the required parameter 'body' when calling"
              + " patchNamespacedSpringCloudGateway(Async)");
    } else {
      Call localVarCall =
          this.patchNamespacedSpringCloudGatewayCall(
              name, namespace, body, pretty, dryRun, fieldManager, _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGateway patchNamespacedSpringCloudGateway(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    ApiResponse<V1SpringCloudGateway> localVarResp =
        this.patchNamespacedSpringCloudGatewayWithHttpInfo(
            name, namespace, body, pretty, dryRun, fieldManager);
    return (V1SpringCloudGateway) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGateway> patchNamespacedSpringCloudGatewayWithHttpInfo(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    Call localVarCall =
        this.patchNamespacedSpringCloudGatewayValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGateway>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call patchNamespacedSpringCloudGatewayAsync(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback<V1SpringCloudGateway> _callback)
      throws ApiException {
    Call localVarCall =
        this.patchNamespacedSpringCloudGatewayValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGateway>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call patchNamespacedSpringCloudGatewayMappingCall(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgatewaymappings/{name}"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (dryRun != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("dryRun", dryRun));
    }

    if (fieldManager != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldManager", fieldManager));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes =
        new String[] {
          "application/json-patch+json",
          "application/merge-patch+json",
          "application/apply-patch+yaml"
        };
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "PATCH",
        localVarQueryParams,
        localVarCollectionQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call patchNamespacedSpringCloudGatewayMappingValidateBeforeCall(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " patchNamespacedSpringCloudGatewayMapping(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " patchNamespacedSpringCloudGatewayMapping(Async)");
    } else if (body == null) {
      throw new ApiException(
          "Missing the required parameter 'body' when calling"
              + " patchNamespacedSpringCloudGatewayMapping(Async)");
    } else {
      Call localVarCall =
          this.patchNamespacedSpringCloudGatewayMappingCall(
              name, namespace, body, pretty, dryRun, fieldManager, _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGatewayMapping patchNamespacedSpringCloudGatewayMapping(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    ApiResponse<V1SpringCloudGatewayMapping> localVarResp =
        this.patchNamespacedSpringCloudGatewayMappingWithHttpInfo(
            name, namespace, body, pretty, dryRun, fieldManager);
    return (V1SpringCloudGatewayMapping) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGatewayMapping>
      patchNamespacedSpringCloudGatewayMappingWithHttpInfo(
          String name,
          String namespace,
          V1Patch body,
          String pretty,
          String dryRun,
          String fieldManager)
          throws ApiException {
    Call localVarCall =
        this.patchNamespacedSpringCloudGatewayMappingValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayMapping>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call patchNamespacedSpringCloudGatewayMappingAsync(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback<V1SpringCloudGatewayMapping> _callback)
      throws ApiException {
    Call localVarCall =
        this.patchNamespacedSpringCloudGatewayMappingValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayMapping>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call patchNamespacedSpringCloudGatewayRouteConfigCall(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgatewayrouteconfigs/{name}"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (dryRun != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("dryRun", dryRun));
    }

    if (fieldManager != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldManager", fieldManager));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes =
        new String[] {
          "application/json-patch+json",
          "application/merge-patch+json",
          "application/apply-patch+yaml"
        };
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "PATCH",
        localVarQueryParams,
        localVarCollectionQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call patchNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " patchNamespacedSpringCloudGatewayRouteConfig(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " patchNamespacedSpringCloudGatewayRouteConfig(Async)");
    } else if (body == null) {
      throw new ApiException(
          "Missing the required parameter 'body' when calling"
              + " patchNamespacedSpringCloudGatewayRouteConfig(Async)");
    } else {
      Call localVarCall =
          this.patchNamespacedSpringCloudGatewayRouteConfigCall(
              name, namespace, body, pretty, dryRun, fieldManager, _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGatewayRouteConfig patchNamespacedSpringCloudGatewayRouteConfig(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    ApiResponse<V1SpringCloudGatewayRouteConfig> localVarResp =
        this.patchNamespacedSpringCloudGatewayRouteConfigWithHttpInfo(
            name, namespace, body, pretty, dryRun, fieldManager);
    return (V1SpringCloudGatewayRouteConfig) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGatewayRouteConfig>
      patchNamespacedSpringCloudGatewayRouteConfigWithHttpInfo(
          String name,
          String namespace,
          V1Patch body,
          String pretty,
          String dryRun,
          String fieldManager)
          throws ApiException {
    Call localVarCall =
        this.patchNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayRouteConfig>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call patchNamespacedSpringCloudGatewayRouteConfigAsync(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback<V1SpringCloudGatewayRouteConfig> _callback)
      throws ApiException {
    Call localVarCall =
        this.patchNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayRouteConfig>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call patchNamespacedSpringCloudGatewayScaleCall(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgateways/{name}/scale"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (dryRun != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("dryRun", dryRun));
    }

    if (fieldManager != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldManager", fieldManager));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes =
        new String[] {
          "application/json-patch+json",
          "application/merge-patch+json",
          "application/apply-patch+yaml"
        };
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "PATCH",
        localVarQueryParams,
        localVarCollectionQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call patchNamespacedSpringCloudGatewayScaleValidateBeforeCall(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " patchNamespacedSpringCloudGatewayScale(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " patchNamespacedSpringCloudGatewayScale(Async)");
    } else if (body == null) {
      throw new ApiException(
          "Missing the required parameter 'body' when calling"
              + " patchNamespacedSpringCloudGatewayScale(Async)");
    } else {
      Call localVarCall =
          this.patchNamespacedSpringCloudGatewayScaleCall(
              name, namespace, body, pretty, dryRun, fieldManager, _callback);
      return localVarCall;
    }
  }

  public V1Scale patchNamespacedSpringCloudGatewayScale(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    ApiResponse<V1Scale> localVarResp =
        this.patchNamespacedSpringCloudGatewayScaleWithHttpInfo(
            name, namespace, body, pretty, dryRun, fieldManager);
    return (V1Scale) localVarResp.getData();
  }

  public ApiResponse<V1Scale> patchNamespacedSpringCloudGatewayScaleWithHttpInfo(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    Call localVarCall =
        this.patchNamespacedSpringCloudGatewayScaleValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1Scale>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call patchNamespacedSpringCloudGatewayScaleAsync(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback<V1Scale> _callback)
      throws ApiException {
    Call localVarCall =
        this.patchNamespacedSpringCloudGatewayScaleValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, _callback);
    Type localVarReturnType = (new TypeToken<V1Scale>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call patchNamespacedSpringCloudGatewayStatusCall(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgateways/{name}/status"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (dryRun != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("dryRun", dryRun));
    }

    if (fieldManager != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldManager", fieldManager));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes =
        new String[] {
          "application/json-patch+json",
          "application/merge-patch+json",
          "application/apply-patch+yaml"
        };
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "PATCH",
        localVarQueryParams,
        localVarCollectionQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call patchNamespacedSpringCloudGatewayStatusValidateBeforeCall(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " patchNamespacedSpringCloudGatewayStatus(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " patchNamespacedSpringCloudGatewayStatus(Async)");
    } else if (body == null) {
      throw new ApiException(
          "Missing the required parameter 'body' when calling"
              + " patchNamespacedSpringCloudGatewayStatus(Async)");
    } else {
      Call localVarCall =
          this.patchNamespacedSpringCloudGatewayStatusCall(
              name, namespace, body, pretty, dryRun, fieldManager, _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGateway patchNamespacedSpringCloudGatewayStatus(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    ApiResponse<V1SpringCloudGateway> localVarResp =
        this.patchNamespacedSpringCloudGatewayStatusWithHttpInfo(
            name, namespace, body, pretty, dryRun, fieldManager);
    return (V1SpringCloudGateway) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGateway> patchNamespacedSpringCloudGatewayStatusWithHttpInfo(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    Call localVarCall =
        this.patchNamespacedSpringCloudGatewayStatusValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGateway>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call patchNamespacedSpringCloudGatewayStatusAsync(
      String name,
      String namespace,
      V1Patch body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback<V1SpringCloudGateway> _callback)
      throws ApiException {
    Call localVarCall =
        this.patchNamespacedSpringCloudGatewayStatusValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGateway>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call readNamespacedSpringCloudGatewayCall(
      String name,
      String namespace,
      String pretty,
      String resourceVersion,
      final ApiCallback _callback)
      throws ApiException {
    Object localVarPostBody = null;
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgateways/{name}"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (resourceVersion != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersion", resourceVersion));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[0];
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "GET",
        localVarQueryParams,
        localVarCollectionQueryParams,
        localVarPostBody,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call readNamespacedSpringCloudGatewayValidateBeforeCall(
      String name,
      String namespace,
      String pretty,
      String resourceVersion,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " readNamespacedSpringCloudGateway(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " readNamespacedSpringCloudGateway(Async)");
    } else {
      Call localVarCall =
          this.readNamespacedSpringCloudGatewayCall(
              name, namespace, pretty, resourceVersion, _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGateway readNamespacedSpringCloudGateway(
      String name, String namespace, String pretty, String resourceVersion) throws ApiException {
    ApiResponse<V1SpringCloudGateway> localVarResp =
        this.readNamespacedSpringCloudGatewayWithHttpInfo(name, namespace, pretty, resourceVersion);
    return (V1SpringCloudGateway) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGateway> readNamespacedSpringCloudGatewayWithHttpInfo(
      String name, String namespace, String pretty, String resourceVersion) throws ApiException {
    Call localVarCall =
        this.readNamespacedSpringCloudGatewayValidateBeforeCall(
            name, namespace, pretty, resourceVersion, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGateway>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call readNamespacedSpringCloudGatewayAsync(
      String name,
      String namespace,
      String pretty,
      String resourceVersion,
      final ApiCallback<V1SpringCloudGateway> _callback)
      throws ApiException {
    Call localVarCall =
        this.readNamespacedSpringCloudGatewayValidateBeforeCall(
            name, namespace, pretty, resourceVersion, _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGateway>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call readNamespacedSpringCloudGatewayMappingCall(
      String name,
      String namespace,
      String pretty,
      String resourceVersion,
      final ApiCallback _callback)
      throws ApiException {
    Object localVarPostBody = null;
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgatewaymappings/{name}"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (resourceVersion != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersion", resourceVersion));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[0];
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "GET",
        localVarQueryParams,
        localVarCollectionQueryParams,
        localVarPostBody,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call readNamespacedSpringCloudGatewayMappingValidateBeforeCall(
      String name,
      String namespace,
      String pretty,
      String resourceVersion,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " readNamespacedSpringCloudGatewayMapping(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " readNamespacedSpringCloudGatewayMapping(Async)");
    } else {
      Call localVarCall =
          this.readNamespacedSpringCloudGatewayMappingCall(
              name, namespace, pretty, resourceVersion, _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGatewayMapping readNamespacedSpringCloudGatewayMapping(
      String name, String namespace, String pretty, String resourceVersion) throws ApiException {
    ApiResponse<V1SpringCloudGatewayMapping> localVarResp =
        this.readNamespacedSpringCloudGatewayMappingWithHttpInfo(
            name, namespace, pretty, resourceVersion);
    return (V1SpringCloudGatewayMapping) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGatewayMapping>
      readNamespacedSpringCloudGatewayMappingWithHttpInfo(
          String name, String namespace, String pretty, String resourceVersion)
          throws ApiException {
    Call localVarCall =
        this.readNamespacedSpringCloudGatewayMappingValidateBeforeCall(
            name, namespace, pretty, resourceVersion, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayMapping>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call readNamespacedSpringCloudGatewayMappingAsync(
      String name,
      String namespace,
      String pretty,
      String resourceVersion,
      final ApiCallback<V1SpringCloudGatewayMapping> _callback)
      throws ApiException {
    Call localVarCall =
        this.readNamespacedSpringCloudGatewayMappingValidateBeforeCall(
            name, namespace, pretty, resourceVersion, _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayMapping>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call readNamespacedSpringCloudGatewayRouteConfigCall(
      String name,
      String namespace,
      String pretty,
      String resourceVersion,
      final ApiCallback _callback)
      throws ApiException {
    Object localVarPostBody = null;
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgatewayrouteconfigs/{name}"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (resourceVersion != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersion", resourceVersion));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[0];
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "GET",
        localVarQueryParams,
        localVarCollectionQueryParams,
        localVarPostBody,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call readNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
      String name,
      String namespace,
      String pretty,
      String resourceVersion,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " readNamespacedSpringCloudGatewayRouteConfig(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " readNamespacedSpringCloudGatewayRouteConfig(Async)");
    } else {
      Call localVarCall =
          this.readNamespacedSpringCloudGatewayRouteConfigCall(
              name, namespace, pretty, resourceVersion, _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGatewayRouteConfig readNamespacedSpringCloudGatewayRouteConfig(
      String name, String namespace, String pretty, String resourceVersion) throws ApiException {
    ApiResponse<V1SpringCloudGatewayRouteConfig> localVarResp =
        this.readNamespacedSpringCloudGatewayRouteConfigWithHttpInfo(
            name, namespace, pretty, resourceVersion);
    return (V1SpringCloudGatewayRouteConfig) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGatewayRouteConfig>
      readNamespacedSpringCloudGatewayRouteConfigWithHttpInfo(
          String name, String namespace, String pretty, String resourceVersion)
          throws ApiException {
    Call localVarCall =
        this.readNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
            name, namespace, pretty, resourceVersion, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayRouteConfig>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call readNamespacedSpringCloudGatewayRouteConfigAsync(
      String name,
      String namespace,
      String pretty,
      String resourceVersion,
      final ApiCallback<V1SpringCloudGatewayRouteConfig> _callback)
      throws ApiException {
    Call localVarCall =
        this.readNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
            name, namespace, pretty, resourceVersion, _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayRouteConfig>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call readNamespacedSpringCloudGatewayScaleCall(
      String name,
      String namespace,
      String pretty,
      String resourceVersion,
      final ApiCallback _callback)
      throws ApiException {
    Object localVarPostBody = null;
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgateways/{name}/scale"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (resourceVersion != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersion", resourceVersion));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[0];
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "GET",
        localVarQueryParams,
        localVarCollectionQueryParams,
        localVarPostBody,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call readNamespacedSpringCloudGatewayScaleValidateBeforeCall(
      String name,
      String namespace,
      String pretty,
      String resourceVersion,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " readNamespacedSpringCloudGatewayScale(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " readNamespacedSpringCloudGatewayScale(Async)");
    } else {
      Call localVarCall =
          this.readNamespacedSpringCloudGatewayScaleCall(
              name, namespace, pretty, resourceVersion, _callback);
      return localVarCall;
    }
  }

  public V1Scale readNamespacedSpringCloudGatewayScale(
      String name, String namespace, String pretty, String resourceVersion) throws ApiException {
    ApiResponse<V1Scale> localVarResp =
        this.readNamespacedSpringCloudGatewayScaleWithHttpInfo(
            name, namespace, pretty, resourceVersion);
    return (V1Scale) localVarResp.getData();
  }

  public ApiResponse<V1Scale> readNamespacedSpringCloudGatewayScaleWithHttpInfo(
      String name, String namespace, String pretty, String resourceVersion) throws ApiException {
    Call localVarCall =
        this.readNamespacedSpringCloudGatewayScaleValidateBeforeCall(
            name, namespace, pretty, resourceVersion, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1Scale>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call readNamespacedSpringCloudGatewayScaleAsync(
      String name,
      String namespace,
      String pretty,
      String resourceVersion,
      final ApiCallback<V1Scale> _callback)
      throws ApiException {
    Call localVarCall =
        this.readNamespacedSpringCloudGatewayScaleValidateBeforeCall(
            name, namespace, pretty, resourceVersion, _callback);
    Type localVarReturnType = (new TypeToken<V1Scale>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call readNamespacedSpringCloudGatewayStatusCall(
      String name,
      String namespace,
      String pretty,
      String resourceVersion,
      final ApiCallback _callback)
      throws ApiException {
    Object localVarPostBody = null;
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgateways/{name}/status"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (resourceVersion != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("resourceVersion", resourceVersion));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[0];
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "GET",
        localVarQueryParams,
        localVarCollectionQueryParams,
        localVarPostBody,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call readNamespacedSpringCloudGatewayStatusValidateBeforeCall(
      String name,
      String namespace,
      String pretty,
      String resourceVersion,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " readNamespacedSpringCloudGatewayStatus(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " readNamespacedSpringCloudGatewayStatus(Async)");
    } else {
      Call localVarCall =
          this.readNamespacedSpringCloudGatewayStatusCall(
              name, namespace, pretty, resourceVersion, _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGateway readNamespacedSpringCloudGatewayStatus(
      String name, String namespace, String pretty, String resourceVersion) throws ApiException {
    ApiResponse<V1SpringCloudGateway> localVarResp =
        this.readNamespacedSpringCloudGatewayStatusWithHttpInfo(
            name, namespace, pretty, resourceVersion);
    return (V1SpringCloudGateway) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGateway> readNamespacedSpringCloudGatewayStatusWithHttpInfo(
      String name, String namespace, String pretty, String resourceVersion) throws ApiException {
    Call localVarCall =
        this.readNamespacedSpringCloudGatewayStatusValidateBeforeCall(
            name, namespace, pretty, resourceVersion, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGateway>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call readNamespacedSpringCloudGatewayStatusAsync(
      String name,
      String namespace,
      String pretty,
      String resourceVersion,
      final ApiCallback<V1SpringCloudGateway> _callback)
      throws ApiException {
    Call localVarCall =
        this.readNamespacedSpringCloudGatewayStatusValidateBeforeCall(
            name, namespace, pretty, resourceVersion, _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGateway>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call replaceNamespacedSpringCloudGatewayCall(
      String name,
      String namespace,
      V1SpringCloudGateway body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgateways/{name}"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (dryRun != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("dryRun", dryRun));
    }

    if (fieldManager != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldManager", fieldManager));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[] {"application/json", "application/yaml"};
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "PUT",
        localVarQueryParams,
        localVarCollectionQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call replaceNamespacedSpringCloudGatewayValidateBeforeCall(
      String name,
      String namespace,
      V1SpringCloudGateway body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " replaceNamespacedSpringCloudGateway(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " replaceNamespacedSpringCloudGateway(Async)");
    } else if (body == null) {
      throw new ApiException(
          "Missing the required parameter 'body' when calling"
              + " replaceNamespacedSpringCloudGateway(Async)");
    } else {
      Call localVarCall =
          this.replaceNamespacedSpringCloudGatewayCall(
              name, namespace, body, pretty, dryRun, fieldManager, _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGateway replaceNamespacedSpringCloudGateway(
      String name,
      String namespace,
      V1SpringCloudGateway body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    ApiResponse<V1SpringCloudGateway> localVarResp =
        this.replaceNamespacedSpringCloudGatewayWithHttpInfo(
            name, namespace, body, pretty, dryRun, fieldManager);
    return (V1SpringCloudGateway) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGateway> replaceNamespacedSpringCloudGatewayWithHttpInfo(
      String name,
      String namespace,
      V1SpringCloudGateway body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    Call localVarCall =
        this.replaceNamespacedSpringCloudGatewayValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGateway>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call replaceNamespacedSpringCloudGatewayAsync(
      String name,
      String namespace,
      V1SpringCloudGateway body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback<V1SpringCloudGateway> _callback)
      throws ApiException {
    Call localVarCall =
        this.replaceNamespacedSpringCloudGatewayValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGateway>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call replaceNamespacedSpringCloudGatewayMappingCall(
      String name,
      String namespace,
      V1SpringCloudGatewayMapping body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgatewaymappings/{name}"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (dryRun != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("dryRun", dryRun));
    }

    if (fieldManager != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldManager", fieldManager));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[] {"application/json", "application/yaml"};
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "PUT",
        localVarQueryParams,
        localVarCollectionQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call replaceNamespacedSpringCloudGatewayMappingValidateBeforeCall(
      String name,
      String namespace,
      V1SpringCloudGatewayMapping body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " replaceNamespacedSpringCloudGatewayMapping(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " replaceNamespacedSpringCloudGatewayMapping(Async)");
    } else if (body == null) {
      throw new ApiException(
          "Missing the required parameter 'body' when calling"
              + " replaceNamespacedSpringCloudGatewayMapping(Async)");
    } else {
      Call localVarCall =
          this.replaceNamespacedSpringCloudGatewayMappingCall(
              name, namespace, body, pretty, dryRun, fieldManager, _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGatewayMapping replaceNamespacedSpringCloudGatewayMapping(
      String name,
      String namespace,
      V1SpringCloudGatewayMapping body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    ApiResponse<V1SpringCloudGatewayMapping> localVarResp =
        this.replaceNamespacedSpringCloudGatewayMappingWithHttpInfo(
            name, namespace, body, pretty, dryRun, fieldManager);
    return (V1SpringCloudGatewayMapping) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGatewayMapping>
      replaceNamespacedSpringCloudGatewayMappingWithHttpInfo(
          String name,
          String namespace,
          V1SpringCloudGatewayMapping body,
          String pretty,
          String dryRun,
          String fieldManager)
          throws ApiException {
    Call localVarCall =
        this.replaceNamespacedSpringCloudGatewayMappingValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayMapping>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call replaceNamespacedSpringCloudGatewayMappingAsync(
      String name,
      String namespace,
      V1SpringCloudGatewayMapping body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback<V1SpringCloudGatewayMapping> _callback)
      throws ApiException {
    Call localVarCall =
        this.replaceNamespacedSpringCloudGatewayMappingValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayMapping>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call replaceNamespacedSpringCloudGatewayRouteConfigCall(
      String name,
      String namespace,
      V1SpringCloudGatewayRouteConfig body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgatewayrouteconfigs/{name}"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (dryRun != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("dryRun", dryRun));
    }

    if (fieldManager != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldManager", fieldManager));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[] {"application/json", "application/yaml"};
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "PUT",
        localVarQueryParams,
        localVarCollectionQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call replaceNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
      String name,
      String namespace,
      V1SpringCloudGatewayRouteConfig body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " replaceNamespacedSpringCloudGatewayRouteConfig(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " replaceNamespacedSpringCloudGatewayRouteConfig(Async)");
    } else if (body == null) {
      throw new ApiException(
          "Missing the required parameter 'body' when calling"
              + " replaceNamespacedSpringCloudGatewayRouteConfig(Async)");
    } else {
      Call localVarCall =
          this.replaceNamespacedSpringCloudGatewayRouteConfigCall(
              name, namespace, body, pretty, dryRun, fieldManager, _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGatewayRouteConfig replaceNamespacedSpringCloudGatewayRouteConfig(
      String name,
      String namespace,
      V1SpringCloudGatewayRouteConfig body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    ApiResponse<V1SpringCloudGatewayRouteConfig> localVarResp =
        this.replaceNamespacedSpringCloudGatewayRouteConfigWithHttpInfo(
            name, namespace, body, pretty, dryRun, fieldManager);
    return (V1SpringCloudGatewayRouteConfig) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGatewayRouteConfig>
      replaceNamespacedSpringCloudGatewayRouteConfigWithHttpInfo(
          String name,
          String namespace,
          V1SpringCloudGatewayRouteConfig body,
          String pretty,
          String dryRun,
          String fieldManager)
          throws ApiException {
    Call localVarCall =
        this.replaceNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayRouteConfig>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call replaceNamespacedSpringCloudGatewayRouteConfigAsync(
      String name,
      String namespace,
      V1SpringCloudGatewayRouteConfig body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback<V1SpringCloudGatewayRouteConfig> _callback)
      throws ApiException {
    Call localVarCall =
        this.replaceNamespacedSpringCloudGatewayRouteConfigValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGatewayRouteConfig>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call replaceNamespacedSpringCloudGatewayScaleCall(
      String name,
      String namespace,
      V1Scale body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgateways/{name}/scale"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (dryRun != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("dryRun", dryRun));
    }

    if (fieldManager != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldManager", fieldManager));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[] {"application/json", "application/yaml"};
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "PUT",
        localVarQueryParams,
        localVarCollectionQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call replaceNamespacedSpringCloudGatewayScaleValidateBeforeCall(
      String name,
      String namespace,
      V1Scale body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " replaceNamespacedSpringCloudGatewayScale(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " replaceNamespacedSpringCloudGatewayScale(Async)");
    } else if (body == null) {
      throw new ApiException(
          "Missing the required parameter 'body' when calling"
              + " replaceNamespacedSpringCloudGatewayScale(Async)");
    } else {
      Call localVarCall =
          this.replaceNamespacedSpringCloudGatewayScaleCall(
              name, namespace, body, pretty, dryRun, fieldManager, _callback);
      return localVarCall;
    }
  }

  public V1Scale replaceNamespacedSpringCloudGatewayScale(
      String name,
      String namespace,
      V1Scale body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    ApiResponse<V1Scale> localVarResp =
        this.replaceNamespacedSpringCloudGatewayScaleWithHttpInfo(
            name, namespace, body, pretty, dryRun, fieldManager);
    return (V1Scale) localVarResp.getData();
  }

  public ApiResponse<V1Scale> replaceNamespacedSpringCloudGatewayScaleWithHttpInfo(
      String name,
      String namespace,
      V1Scale body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    Call localVarCall =
        this.replaceNamespacedSpringCloudGatewayScaleValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1Scale>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call replaceNamespacedSpringCloudGatewayScaleAsync(
      String name,
      String namespace,
      V1Scale body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback<V1Scale> _callback)
      throws ApiException {
    Call localVarCall =
        this.replaceNamespacedSpringCloudGatewayScaleValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, _callback);
    Type localVarReturnType = (new TypeToken<V1Scale>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }

  public Call replaceNamespacedSpringCloudGatewayStatusCall(
      String name,
      String namespace,
      V1SpringCloudGateway body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    String localVarPath =
        "/apis/tanzu.vmware.com/v1/namespaces/{namespace}/springcloudgateways/{name}/status"
            .replaceAll("\\{name\\}", this.localVarApiClient.escapeString(name.toString()))
            .replaceAll(
                "\\{namespace\\}", this.localVarApiClient.escapeString(namespace.toString()));
    List<Pair> localVarQueryParams = new ArrayList<>();
    List<Pair> localVarCollectionQueryParams = new ArrayList<>();
    if (pretty != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("pretty", pretty));
    }

    if (dryRun != null) {
      localVarQueryParams.addAll(this.localVarApiClient.parameterToPair("dryRun", dryRun));
    }

    if (fieldManager != null) {
      localVarQueryParams.addAll(
          this.localVarApiClient.parameterToPair("fieldManager", fieldManager));
    }

    Map<String, String> localVarHeaderParams = new HashMap<>();
    Map<String, String> localVarCookieParams = new HashMap<>();
    Map<String, Object> localVarFormParams = new HashMap<>();
    String[] localVarAccepts = new String[] {"application/json", "application/yaml"};
    String localVarAccept = this.localVarApiClient.selectHeaderAccept(localVarAccepts);
    if (localVarAccept != null) {
      localVarHeaderParams.put("Accept", localVarAccept);
    }

    String[] localVarContentTypes = new String[] {"application/json", "application/yaml"};
    String localVarContentType =
        this.localVarApiClient.selectHeaderContentType(localVarContentTypes);
    localVarHeaderParams.put("Content-Type", localVarContentType);
    String[] localVarAuthNames = new String[] {"BearerToken"};
    return this.localVarApiClient.buildCall(
        localVarPath,
        "PUT",
        localVarQueryParams,
        localVarCollectionQueryParams,
        body,
        localVarHeaderParams,
        localVarCookieParams,
        localVarFormParams,
        localVarAuthNames,
        _callback);
  }

  private Call replaceNamespacedSpringCloudGatewayStatusValidateBeforeCall(
      String name,
      String namespace,
      V1SpringCloudGateway body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback _callback)
      throws ApiException {
    if (name == null) {
      throw new ApiException(
          "Missing the required parameter 'name' when calling"
              + " replaceNamespacedSpringCloudGatewayStatus(Async)");
    } else if (namespace == null) {
      throw new ApiException(
          "Missing the required parameter 'namespace' when calling"
              + " replaceNamespacedSpringCloudGatewayStatus(Async)");
    } else if (body == null) {
      throw new ApiException(
          "Missing the required parameter 'body' when calling"
              + " replaceNamespacedSpringCloudGatewayStatus(Async)");
    } else {
      Call localVarCall =
          this.replaceNamespacedSpringCloudGatewayStatusCall(
              name, namespace, body, pretty, dryRun, fieldManager, _callback);
      return localVarCall;
    }
  }

  public V1SpringCloudGateway replaceNamespacedSpringCloudGatewayStatus(
      String name,
      String namespace,
      V1SpringCloudGateway body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    ApiResponse<V1SpringCloudGateway> localVarResp =
        this.replaceNamespacedSpringCloudGatewayStatusWithHttpInfo(
            name, namespace, body, pretty, dryRun, fieldManager);
    return (V1SpringCloudGateway) localVarResp.getData();
  }

  public ApiResponse<V1SpringCloudGateway> replaceNamespacedSpringCloudGatewayStatusWithHttpInfo(
      String name,
      String namespace,
      V1SpringCloudGateway body,
      String pretty,
      String dryRun,
      String fieldManager)
      throws ApiException {
    Call localVarCall =
        this.replaceNamespacedSpringCloudGatewayStatusValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, (ApiCallback) null);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGateway>() {}).getType();
    return this.localVarApiClient.execute(localVarCall, localVarReturnType);
  }

  public Call replaceNamespacedSpringCloudGatewayStatusAsync(
      String name,
      String namespace,
      V1SpringCloudGateway body,
      String pretty,
      String dryRun,
      String fieldManager,
      final ApiCallback<V1SpringCloudGateway> _callback)
      throws ApiException {
    Call localVarCall =
        this.replaceNamespacedSpringCloudGatewayStatusValidateBeforeCall(
            name, namespace, body, pretty, dryRun, fieldManager, _callback);
    Type localVarReturnType = (new TypeToken<V1SpringCloudGateway>() {}).getType();
    this.localVarApiClient.executeAsync(localVarCall, localVarReturnType, _callback);
    return localVarCall;
  }
}
