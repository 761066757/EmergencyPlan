################################################################################
# 阶段1：下载依赖
FROM eclipse-temurin:17-jdk-jammy AS deps

WORKDIR /build

COPY mvnw mvnw
COPY .mvn/ .mvn/
RUN chmod +x mvnw

COPY pom.xml pom.xml
RUN ./mvnw dependency:go-offline -DskipTests

################################################################################
# 阶段2：编译打包（分离式结构：app.jar + lib/ + config/）
FROM deps AS package

WORKDIR /build

COPY ./src src/
RUN ./mvnw package -DskipTests && \
    mv target/$(./mvnw help:evaluate -Dexpression=project.artifactId -q -DforceStdout)-$(./mvnw help:evaluate -Dexpression=project.version -q -DforceStdout).jar target/app.jar

################################################################################
# 阶段3：运行镜像（最小运行时依赖）
FROM eclipse-temurin:17-jre-jammy AS final

WORKDIR /app

# 复制分离式打包产物：主JAR + 依赖库 + 配置文件
COPY --from=package /build/target/app.jar .
COPY --from=package /build/target/lib ./lib
COPY --from=package /build/target/config ./config

EXPOSE 3278
EXPOSE 1633

ENTRYPOINT [ "java", "-jar", "app.jar" ]
