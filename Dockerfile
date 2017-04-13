FROM fabric8/java-jboss-openjdk8-jdk:1.2.3

ENV JAVA_APP_JAR aloha-fat.jar
ENV AB_ENABLED off
ENV AB_JOLOKIA_AUTH_OPENSHIFT true
ENV JAVA_OPTIONS -Xmx256m 
ENV ZIPKIN_SERVER_URL http://zipkin
ENV JAEGER_SERVER_URL jaeger-all-in-one
ENV TRACING_SYSTEM none

EXPOSE 8080

RUN chmod -R 777 /deployments/
ADD target/aloha-fat.jar /deployments/
