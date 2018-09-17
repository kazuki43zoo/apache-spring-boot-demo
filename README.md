# apache-spring-boot-demo

This demo indicates how to configure the reverse proxy to backend application(context path is exists)
on Tomcat using AJP connector for specific path(e.g. `/hello`).

```
/usr/local/apache2/htdocs (Document root for 'http://localhost:10080/')
  ├── index.html
  ├── error
  │     └── 500.html
  └── hello (application path for spring-boot) [1]
```

[1] Does not exists directory and files. This resource will response by tha Tomcat(Spring Boot application) using AJP connector.

## Requirements

* Installed the JDK 8+
* Installed the Docker(Docker Compose)
* Internet access

## Servers

「Apache(10080:80)」  <-----(AJP)----->  「Tomcat(8009)[2]」

[2] Can access 18080 port using HTTP protocol

### Tomcat

* Version: 8.5 (Spring Boot 2.0)
* Local Port : AJP:8009,HTTP:8080(default)
* External Port for docker container : AJP:NONE, HTTP:18080
* Context Path : /app

#### Configure the AJP connector

```java
@SpringBootApplication
public class ApacheSpringBootDemoApplication {
    // ...
    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        Connector connector = new Connector("org.apache.coyote.ajp.AjpNioProtocol");
        connector.setAttribute("maxThreads", 100);
        connector.setPort(8009);
        connector.setRedirectPort(8043);
        connector.setURIEncoding("UTF-8");
        factory.addAdditionalTomcatConnectors(connector);
        return factory;
    }
    // ...
}
```

#### Configure the context path

src/main/resources/application.properties

```properties
server.servlet.context-path=/app
```

#### Add endpoints(application paths)

```java
@SpringBootApplication
public class ApacheSpringBootDemoApplication {

  // ...

  @Controller
  @RequestMapping("/hello")
  static class Hello {

    @GetMapping
    String hello() {
      return "redirect:/hello/foo"; // Redirect to another application path via apache
    }

    @GetMapping("{name}")
    @ResponseBody
    String hello(@PathVariable String name) {
      return "hello " + name + " !"; // Response resource
    }

    @GetMapping("root")
    String root() {
      return "redirect:/"; // Redirect to root path on apache
    }

    @GetMapping("error")
    String error() {
      return "redirect:/error/500.html"; // Redirect to resource path managed by apache
    }

  }

}
```

### Apache

* Version: 2.4
* Local Port : 80 (default)
* External Port for docker container : 10080

### Proxy Settings

The `/hello` or sub resources send to `ajp://ap:8009/app/hello` using reverse proxy.

apache/httpd.conf(/usr/local/apache2/conf/httpd.conf)

```
ProxyRequests Off
ProxyPass /hello ajp://ap:8009/app/hello
ProxyPassReverse / /app/
```

## Build Spring Boot application

```
$ ./mvnw clean package
```

## Start up servers

```
$ docker-compose up --build
```

```
Building ap
Step 1/5 : FROM java:8
 ---> d23bdf5b1b1b
Step 2/5 : ADD target/apache-spring-boot-demo-0.0.1-SNAPSHOT.jar /opt/spring/apache-spring-boot-demo.jar
 ---> 1ecdb08692f4
Step 3/5 : EXPOSE 8080
 ---> Running in daa51ff86cca
Removing intermediate container daa51ff86cca
 ---> 34afd4d2d72c
Step 4/5 : WORKDIR /opt/spring/
 ---> Running in 9b31153d1117
Removing intermediate container 9b31153d1117
 ---> 830ddc97f3bf
Step 5/5 : ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/urandom", "-jar", "apache-spring-boot-demo.jar"]
 ---> Running in 09d6125c264d
Removing intermediate container 09d6125c264d
 ---> 55803c20b68b
Successfully built 55803c20b68b
Successfully tagged apache-spring-boot-demo_ap:latest
Building apache-httpd
Step 1/3 : FROM httpd:2.4
 ---> d595a4011ae3
Step 2/3 : COPY ./httpd.conf /usr/local/apache2/conf/httpd.conf
 ---> 7ab73c0c1d24
Step 3/3 : COPY ./htdocs /usr/local/apache2/htdocs
 ---> 6f3262176a2a
Successfully built 6f3262176a2a
Successfully tagged apache-spring-boot-demo_apache-httpd:latest
Recreating apache-spring-boot-demo_ap_1 ... done
Recreating apache-spring-boot-demo_apache-httpd_1 ... done
Attaching to apache-spring-boot-demo_ap_1, apache-spring-boot-demo_apache-httpd_1
apache-httpd_1  | AH00558: httpd: Could not reliably determine the server's fully qualified domain name, using 172.17.0.3. Set the 'ServerName' directive globally to suppress this message
apache-httpd_1  | AH00558: httpd: Could not reliably determine the server's fully qualified domain name, using 172.17.0.3. Set the 'ServerName' directive globally to suppress this message
apache-httpd_1  | [Mon Sep 17 11:02:23.297152 2018] [mpm_event:notice] [pid 1:tid 140224708573056] AH00489: Apache/2.4.34 (Unix) configured -- resuming normal operations
apache-httpd_1  | [Mon Sep 17 11:02:23.297326 2018] [core:notice] [pid 1:tid 140224708573056] AH00094: Command line: 'httpd -D FOREGROUND'
ap_1            | 
ap_1            |   .   ____          _            __ _ _
ap_1            |  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
ap_1            | ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
ap_1            |  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
ap_1            |   '  |____| .__|_| |_|_| |_\__, | / / / /
ap_1            |  =========|_|==============|___/=/_/_/_/
ap_1            |  :: Spring Boot ::        (v2.0.5.RELEASE)
ap_1            | 
ap_1            | 2018-09-17 11:02:23.600  INFO 1 --- [           main] c.e.a.ApacheSpringBootDemoApplication    : Starting ApacheSpringBootDemoApplication v0.0.1-SNAPSHOT on 1d360873f4a1 with PID 1 (/opt/spring/apache-spring-boot-demo.jar started by root in /opt/spring)
ap_1            | 2018-09-17 11:02:23.604  INFO 1 --- [           main] c.e.a.ApacheSpringBootDemoApplication    : No active profile set, falling back to default profiles: default
ap_1            | 2018-09-17 11:02:23.681  INFO 1 --- [           main] ConfigServletWebServerApplicationContext : Refreshing org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext@6aceb1a5: startup date [Mon Sep 17 11:02:23 UTC 2018]; root of context hierarchy
ap_1            | 2018-09-17 11:02:25.721  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8081 (http) 8009 (http)
ap_1            | 2018-09-17 11:02:25.765  INFO 1 --- [           main] org.apache.coyote.ajp.AjpNioProtocol     : Initializing ProtocolHandler ["ajp-nio-8009"]
ap_1            | 2018-09-17 11:02:25.778  INFO 1 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
ap_1            | 2018-09-17 11:02:25.779  INFO 1 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet Engine: Apache Tomcat/8.5.34
ap_1            | 2018-09-17 11:02:25.803  INFO 1 --- [ost-startStop-1] o.a.catalina.core.AprLifecycleListener   : The APR based Apache Tomcat Native library which allows optimal performance in production environments was not found on the java.library.path: [/usr/java/packages/lib/amd64:/usr/lib/x86_64-linux-gnu/jni:/lib/x86_64-linux-gnu:/usr/lib/x86_64-linux-gnu:/usr/lib/jni:/lib:/usr/lib]
ap_1            | 2018-09-17 11:02:25.932  INFO 1 --- [ost-startStop-1] o.a.c.c.C.[Tomcat].[localhost].[/app]    : Initializing Spring embedded WebApplicationContext
ap_1            | 2018-09-17 11:02:25.933  INFO 1 --- [ost-startStop-1] o.s.web.context.ContextLoader            : Root WebApplicationContext: initialization completed in 2255 ms
ap_1            | 2018-09-17 11:02:26.026  INFO 1 --- [ost-startStop-1] o.s.b.w.servlet.ServletRegistrationBean  : Servlet dispatcherServlet mapped to [/]
ap_1            | 2018-09-17 11:02:26.031  INFO 1 --- [ost-startStop-1] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'characterEncodingFilter' to: [/*]
ap_1            | 2018-09-17 11:02:26.032  INFO 1 --- [ost-startStop-1] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'hiddenHttpMethodFilter' to: [/*]
ap_1            | 2018-09-17 11:02:26.033  INFO 1 --- [ost-startStop-1] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'httpPutFormContentFilter' to: [/*]
ap_1            | 2018-09-17 11:02:26.033  INFO 1 --- [ost-startStop-1] o.s.b.w.servlet.FilterRegistrationBean   : Mapping filter: 'requestContextFilter' to: [/*]
ap_1            | 2018-09-17 11:02:26.233  INFO 1 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/**/favicon.ico] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
ap_1            | 2018-09-17 11:02:26.584  INFO 1 --- [           main] s.w.s.m.m.a.RequestMappingHandlerAdapter : Looking for @ControllerAdvice: org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext@6aceb1a5: startup date [Mon Sep 17 11:02:23 UTC 2018]; root of context hierarchy
ap_1            | 2018-09-17 11:02:26.672  INFO 1 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/hello/{name}],methods=[GET]}" onto java.lang.String com.example.apachespringbootdemo.ApacheSpringBootDemoApplication$Hello.hello(java.lang.String)
ap_1            | 2018-09-17 11:02:26.674  INFO 1 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/hello],methods=[GET]}" onto java.lang.String com.example.apachespringbootdemo.ApacheSpringBootDemoApplication$Hello.hello()
ap_1            | 2018-09-17 11:02:26.675  INFO 1 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/hello/root],methods=[GET]}" onto java.lang.String com.example.apachespringbootdemo.ApacheSpringBootDemoApplication$Hello.root()
ap_1            | 2018-09-17 11:02:26.676  INFO 1 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/hello/error],methods=[GET]}" onto java.lang.String com.example.apachespringbootdemo.ApacheSpringBootDemoApplication$Hello.error()
ap_1            | 2018-09-17 11:02:26.676  INFO 1 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/hello/help],methods=[GET]}" onto java.lang.String com.example.apachespringbootdemo.ApacheSpringBootDemoApplication$Hello.help()
ap_1            | 2018-09-17 11:02:26.681  INFO 1 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/error],produces=[text/html]}" onto public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)
ap_1            | 2018-09-17 11:02:26.682  INFO 1 --- [           main] s.w.s.m.m.a.RequestMappingHandlerMapping : Mapped "{[/error]}" onto public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController.error(javax.servlet.http.HttpServletRequest)
ap_1            | 2018-09-17 11:02:26.721  INFO 1 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/webjars/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
ap_1            | 2018-09-17 11:02:26.722  INFO 1 --- [           main] o.s.w.s.handler.SimpleUrlHandlerMapping  : Mapped URL path [/**] onto handler of type [class org.springframework.web.servlet.resource.ResourceHttpRequestHandler]
ap_1            | 2018-09-17 11:02:26.778  WARN 1 --- [           main] ion$DefaultTemplateResolverConfiguration : Cannot find template location: classpath:/templates/ (please add some templates or check your Thymeleaf configuration)
ap_1            | 2018-09-17 11:02:26.954  INFO 1 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
ap_1            | 2018-09-17 11:02:26.994  INFO 1 --- [           main] org.apache.coyote.ajp.AjpNioProtocol     : Starting ProtocolHandler ["ajp-nio-8009"]
ap_1            | 2018-09-17 11:02:27.003  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8081 (http) 8009 (http) with context path '/app'
ap_1            | 2018-09-17 11:02:27.008  INFO 1 --- [           main] c.e.a.ApacheSpringBootDemoApplication    : Started ApacheSpringBootDemoApplication in 3.913 seconds (JVM running for 4.489)
```

## Access resources

### Root path for Apache

This access is confirmed basic settings for apache http server.

```
$ curl -D - -s -L http://localhost:10080/
```

```http
HTTP/1.1 200 OK
Date: Mon, 17 Sep 2018 11:04:48 GMT
Server: Apache/2.4.34 (Unix)
Last-Modified: Mon, 11 Jun 2007 18:53:14 GMT
ETag: "2d-432a5e4a73a80"
Accept-Ranges: bytes
Content-Length: 45
Content-Type: text/html

<html><body><h1>It works!</h1></body></html>
```

Traffic sequences are:

* Client ----> Apache ----> Client

### Application path via Apache

This access is confirmed settings for proxy.

```
$ curl -D - -s -L http://localhost:10080/hello
```

```http
HTTP/1.1 302 302
Date: Mon, 17 Sep 2018 11:29:25 GMT
Server: Apache/2.4.34 (Unix)
Location: http://localhost:10080/hello/foo
Content-Language: en
Content-Length: 0

HTTP/1.1 200 200
Date: Mon, 17 Sep 2018 11:29:25 GMT
Server: Apache/2.4.34 (Unix)
Content-Type: text/plain;charset=UTF-8
Content-Length: 11

hello foo !
```

Traffic sequences are:

* Client ----> Apache --(Proxy)--> **Tomcat** ----> Apache ----> Client
* Client --(Redirect)--> Apache --(Proxy)--> **Tomcat** ----> Apache ----> Client


### Apache managed path using redirect from application

This access is confirmed settings for reverse proxy.

```
$ curl -D - -s -L http://localhost:10080/hello/error
```

```http
HTTP/1.1 302 302
Date: Mon, 17 Sep 2018 11:38:35 GMT
Server: Apache/2.4.34 (Unix)
Location: http://localhost:10080/error/500.html
Content-Language: en
Content-Length: 0

HTTP/1.1 200 OK
Date: Mon, 17 Sep 2018 11:38:35 GMT
Server: Apache/2.4.34 (Unix)
Last-Modified: Mon, 17 Sep 2018 10:49:59 GMT
ETag: "5a-5760eef3e23c0"
Accept-Ranges: bytes
Content-Length: 90
Content-Type: text/html

<html>
<head>
    <title>server error</title>
</head>
<body>
Server Error.
</body>
</html>
```

Traffic sequences are:

* Client ----> Apache --(Proxy)--> **Tomcat** ----> Apache ----> Client
* Client --(Redirect)--> Apache ----> Client

