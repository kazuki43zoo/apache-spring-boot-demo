FROM java:8
ADD target/apache-spring-boot-demo-0.0.1-SNAPSHOT.jar /opt/spring/apache-spring-boot-demo.jar
EXPOSE 8080
WORKDIR /opt/spring/
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/urandom", "-jar", "apache-spring-boot-demo.jar"]

