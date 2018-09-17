package com.example.apachespringbootdemo;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@SpringBootApplication
public class ApacheSpringBootDemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(ApacheSpringBootDemoApplication.class, args);
  }

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

  @Controller
  @RequestMapping("/hello")
  static class Hello {

    @GetMapping
    String hello(RedirectAttributes attributes, @RequestParam(required = false) String word) {
      attributes.addFlashAttribute("text", word);
      attributes.addAttribute("t", System.currentTimeMillis());
      return "redirect:/hello/foo";
    }

    @GetMapping("{name}")
    @ResponseBody
    String hello(@PathVariable String name, @ModelAttribute("text") String text) {
      return "hello " + name + " with " + text + " !";
    }

    @GetMapping("root")
    String root() {
      return "redirect:/";
    }

    @GetMapping("help")
    String help() {
      return "redirect:/help/help.html";
    }

    @GetMapping("error")
    String error() {
      return "redirect:/error/500.html";
    }

  }

}
