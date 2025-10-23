FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# 拷贝源码并编译
COPY src ./src
RUN mkdir -p /app/classes && \
    javac -d /app/classes $(find /app/src -name "*.java")

# Cloud Run 会注入 PORT 环境变量
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080
CMD ["sh", "-c", "java -cp /app/classes com.example.App"]