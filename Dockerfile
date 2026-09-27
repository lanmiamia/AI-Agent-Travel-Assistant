# 使用轻量级 JDK21 运行环境
FROM openjdk:21-slim

# 工作目录
WORKDIR /app

# 复制已经打包好的JAR文件（假设已放在当前目录）
COPY target/YUP_Agent-0.0.1-SNAPSHOT.jar app.jar

# 暴露应用端口
EXPOSE 8123

# 使用生产环境配置启动应用
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=local", "--spring.ai.mcp.client.sse.connections.server1.url=http://cmrwdwqp.yup-agent-server8127.0vih2o9u.c9xu49kp.com:8127"]
