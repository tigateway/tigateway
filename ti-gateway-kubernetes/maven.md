# 清除失败的缓存
mvn dependency:purge-local-repository -DmanualInclude="com.github.vladimir-bukhtoyarov:bucket4j-core:7.6.0"

# 强制更新依赖
mvn dependency:resolve -U

# 编译目标模块
mvn compile -pl ti-gateway-kubernetes -am