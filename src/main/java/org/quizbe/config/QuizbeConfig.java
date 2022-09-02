package org.quizbe.config;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.quizbe.dao.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
public class QuizbeConfig implements WebMvcConfigurer {

  Logger logger = LoggerFactory.getLogger(QuizbeConfig.class);

  @Autowired
  private UserRepository userRepository;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new QuizbeInterceptor());
    //WebMvcConfigurer.super.addInterceptors(registry);
  }

  @Bean
  public ClassLoaderTemplateResolver primaryTemplateResolver() {

    ClassLoaderTemplateResolver primaryTemplateResolver = new ClassLoaderTemplateResolver();

    primaryTemplateResolver.setPrefix("templates/");
    primaryTemplateResolver.setSuffix(".html");
    primaryTemplateResolver.setTemplateMode(TemplateMode.HTML);
    primaryTemplateResolver.setCharacterEncoding("UTF-8");
    primaryTemplateResolver.setOrder(1);
    primaryTemplateResolver.setCheckExistence(true);

    return primaryTemplateResolver;

  }

//
//  @Bean
//  public TomcatEmbeddedServletContainerFactory tomcatEmbeddedServletContainerFactory() {
//  TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory() {
//    @Override
//    protected void postProcessContext(Context context) {
//      SecurityConstraint securityConstraint = new SecurityConstraint();
//      securityConstraint.setUserConstraint("CONFIDENTIAL");
//      SecurityCollection collection = new SecurityCollection();
//      collection.addPattern("/*");
//      securityConstraint.addCollection(collection);
//      context.addConstraint(securityConstraint);
//    }
//  };
//  return tomcat;
//  }


  //
//  @Bean
//  public SecurityWebFilterChain securityWebFilterChain(
//          ServerHttpSecurity http) {
//    return http.authorizeExchange()
//            .pathMatchers("/actuator/**").permitAll()
//            .anyExchange().authenticated()
//            .and().build();
//  }

}
