package ti.gateway.operator.springcloudgateway.gateway;

import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGateway;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import io.kubernetes.client.openapi.models.V1Role;
import io.kubernetes.client.openapi.models.V1RoleBinding;
import io.kubernetes.client.openapi.models.V1RoleBindingBuilder;
import io.kubernetes.client.openapi.models.V1RoleBuilder;
import io.kubernetes.client.openapi.models.V1ServiceAccount;
import io.kubernetes.client.openapi.models.V1ServiceAccountBuilder;
import io.kubernetes.client.openapi.models.V1RoleBindingFluent.RoleRefNested;
import io.kubernetes.client.openapi.models.V1RoleBindingFluent.SubjectsNested;
import io.kubernetes.client.openapi.models.V1RoleFluent.RulesNested;
import io.kubernetes.client.openapi.models.V1ServiceAccountFluent.MetadataNested;
import java.util.Set;
import org.springframework.util.StringUtils;

public class RbacBuilder {
    private final CoreV1Api coreV1Api;
    private final RbacAuthorizationV1Api rbacAuthorizationApi;

    public RbacBuilder(CoreV1Api coreV1Api, RbacAuthorizationV1Api authorizationApi) {
        this.coreV1Api = coreV1Api;
        this.rbacAuthorizationApi = authorizationApi;
    }

    void create(V1SpringCloudGateway gateway, String namespace) throws ApiException {
        V1ServiceAccount serviceAccount = this.createServiceAccount(gateway, namespace);
        V1Role role = this.createRole(gateway, namespace);
        this.createRoleBinding(gateway, namespace, serviceAccount.getMetadata().getName(), role.getMetadata().getName());
    }

    void update(V1SpringCloudGateway gateway, String namespace, String existingServiceAccountName) throws ApiException {
        V1ServiceAccount serviceAccount = this.buildServiceAccount(gateway, namespace);
        V1Role role = this.buildRole(gateway, namespace);
        String serviceAccountName = serviceAccount.getMetadata().getName();
        String roleName = role.getMetadata().getName();
        V1RoleBinding roleBinding = this.buildRoleBinding(gateway, namespace, serviceAccountName, roleName);
        if (existingServiceAccountName != null && existingServiceAccountName.equals(buildServiceAccountName(gateway))) {
            this.coreV1Api.replaceNamespacedServiceAccount(serviceAccountName, namespace, serviceAccount, (String)null, (String)null, (String)null);
        } else {
            this.createServiceAccount(gateway, namespace);
        }

        this.rbacAuthorizationApi.replaceNamespacedRole(roleName, namespace, role, (String)null, (String)null, (String)null);
        this.rbacAuthorizationApi.replaceNamespacedRoleBinding(roleBinding.getMetadata().getName(), namespace, roleBinding, (String)null, (String)null, (String)null);
    }

    static String buildServiceAccountName(V1SpringCloudGateway gateway) {
        return gateway.getSpec() != null && gateway.getSpec().getServiceAccount() != null && StringUtils.hasText(gateway.getSpec().getServiceAccount().getName()) ? gateway.getSpec().getServiceAccount().getName() : gateway.getMetadata().getName() + "-svc-acc";
    }

    static String buildRoleName(V1SpringCloudGateway gateway) {
        return gateway.getMetadata().getName() + "-role";
    }

    static String buildRoleBindingName(V1SpringCloudGateway gateway) {
        return gateway.getMetadata().getName() + "-role-binding";
    }

    private V1ServiceAccount createServiceAccount(V1SpringCloudGateway gateway, String namespace) throws ApiException {
        V1ServiceAccount serviceAccount = this.buildServiceAccount(gateway, namespace);
        CoreV1Api var10000 = this.coreV1Api;
        String var10005 = buildServiceAccountName(gateway);
        boolean serviceAccountExists = var10000.listNamespacedServiceAccount(namespace, (String)null, (Boolean)null, (String)null, "metadata.name=" + var10005, (String)null, (Integer)null, (String)null, (String)null, (Integer)null, (Boolean)null).getItems().size() > 0;
        if (!serviceAccountExists) {
            this.coreV1Api.createNamespacedServiceAccount(namespace, serviceAccount, (String)null, (String)null, (String)null);
        }

        return serviceAccount;
    }

    private V1ServiceAccount buildServiceAccount(V1SpringCloudGateway gateway, String namespace) {
        return ((V1ServiceAccountBuilder)((MetadataNested)((MetadataNested)((MetadataNested)(new V1ServiceAccountBuilder()).withNewMetadata().withName(buildServiceAccountName(gateway))).withNamespace(namespace)).addToOwnerReferences(new V1OwnerReference[]{SpringCloudGatewayReconciler.createOwnerReference(gateway)})).endMetadata()).build();
    }

    private V1RoleBinding createRoleBinding(V1SpringCloudGateway gateway, String namespace, String serviceAccountName, String roleName) throws ApiException {
        V1RoleBinding roleBinding = this.buildRoleBinding(gateway, namespace, serviceAccountName, roleName);
        RbacAuthorizationV1Api var10000 = this.rbacAuthorizationApi;
        String var10005 = buildRoleBindingName(gateway);
        boolean roleBindingExists = var10000.listNamespacedRoleBinding(namespace, (String)null, (Boolean)null, (String)null, "metadata.name=" + var10005, (String)null, (Integer)null, (String)null, (String)null, (Integer)null, (Boolean)null).getItems().size() > 0;
        if (!roleBindingExists) {
            this.rbacAuthorizationApi.createNamespacedRoleBinding(namespace, roleBinding, (String)null, (String)null, (String)null);
        }

        return roleBinding;
    }

    private V1RoleBinding buildRoleBinding(V1SpringCloudGateway gateway, String namespace, String serviceAccountName, String roleName) {
        return ((V1RoleBindingBuilder)((SubjectsNested)((SubjectsNested)((V1RoleBindingBuilder)((RoleRefNested)((RoleRefNested)((RoleRefNested)((V1RoleBindingBuilder)((io.kubernetes.client.openapi.models.V1RoleBindingFluent.MetadataNested)((io.kubernetes.client.openapi.models.V1RoleBindingFluent.MetadataNested)((io.kubernetes.client.openapi.models.V1RoleBindingFluent.MetadataNested)(new V1RoleBindingBuilder()).withNewMetadata().withName(buildRoleBindingName(gateway))).withNamespace(namespace)).addToOwnerReferences(new V1OwnerReference[]{SpringCloudGatewayReconciler.createOwnerReference(gateway)})).endMetadata()).withNewRoleRef().withApiGroup("rbac.authorization.k8s.io")).withKind("Role")).withName(roleName)).endRoleRef()).addNewSubject().withName(serviceAccountName)).withKind("ServiceAccount")).endSubject()).build();
    }

    private V1Role createRole(V1SpringCloudGateway gateway, String namespace) throws ApiException {
        V1Role role = this.buildRole(gateway, namespace);
        RbacAuthorizationV1Api var10000 = this.rbacAuthorizationApi;
        String var10005 = buildRoleName(gateway);
        boolean roleExists = var10000.listNamespacedRole(namespace, (String)null, (Boolean)null, (String)null, "metadata.name=" + var10005, (String)null, (Integer)null, (String)null, (String)null, (Integer)null, (Boolean)null).getItems().size() > 0;
        if (!roleExists) {
            this.rbacAuthorizationApi.createNamespacedRole(namespace, role, (String)null, (String)null, (String)null);
        }

        return role;
    }

    private V1Role buildRole(V1SpringCloudGateway gateway, String namespace) {
        return ((V1RoleBuilder)((RulesNested)((RulesNested)((RulesNested)((V1RoleBuilder)((RulesNested)((RulesNested)((RulesNested)((V1RoleBuilder)((io.kubernetes.client.openapi.models.V1RoleFluent.MetadataNested)((io.kubernetes.client.openapi.models.V1RoleFluent.MetadataNested)((io.kubernetes.client.openapi.models.V1RoleFluent.MetadataNested)(new V1RoleBuilder()).withNewMetadata().withName(buildRoleName(gateway))).withNamespace(namespace)).addToOwnerReferences(new V1OwnerReference[]{SpringCloudGatewayReconciler.createOwnerReference(gateway)})).endMetadata()).addNewRule().addNewApiGroup("")).addAllToResources(Set.of("endpoints", "services"))).addAllToVerbs(Set.of("get", "list"))).endRule()).addNewRule().addNewApiGroup("discovery.k8s.io")).addAllToResources(Set.of("endpointslices"))).addAllToVerbs(Set.of("get", "list"))).endRule()).build();
    }
}

