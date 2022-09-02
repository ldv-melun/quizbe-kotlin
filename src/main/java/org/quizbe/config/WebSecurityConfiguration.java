package org.quizbe.config;

import org.quizbe.service.CustomUserServiceDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.security.config.Customizer.withDefaults;


@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

  @Autowired
  private CustomUserServiceDetails userDetailsService;

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth
            .userDetailsService(userDetailsService)
            .passwordEncoder(bCryptPasswordEncoder);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {

    http.
            authorizeRequests()
            .antMatchers("/").permitAll()
            .antMatchers("/login").permitAll()
            .antMatchers("/register").permitAll()
            .antMatchers("/webjars/**").permitAll()
            .antMatchers("/error").permitAll()
            //.antMatchers("/admin/**").permitAll()
            .antMatchers("/question/**").hasAnyAuthority("USER")
            .antMatchers("/user/**").hasAuthority("USER")
            .antMatchers("/douser/**").hasAuthority("CHANGE_PW")
//            .antMatchers("/topic/**").hasAuthority("TEACHER")
            .antMatchers("/admin/**").hasAuthority("ADMIN").anyRequest()
            .authenticated()
            .and()
            //.csrf().disable()
            .formLogin()
              .loginPage("/login").failureUrl("/login?error=true")
              .defaultSuccessUrl("/question?login")
              .usernameParameter("username")
              .passwordParameter("password")
            .and()
            .logout()
              .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
              .logoutSuccessUrl("/login")
            .and()
            .exceptionHandling().accessDeniedHandler(accessDeniedHandler());
            // .exceptionHandling().accessDeniedPage("/access-denied");
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    web
            .ignoring()
            .antMatchers("/resources/**", "/svg/**", "/images/**", "/static/**", "/css/**", "/js/**", "/images/**, /console/**" +
                    "");
  }

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public UserDetailsService userDetailsService() {
    return new CustomUserServiceDetails();
  }

  @Bean
  GrantedAuthorityDefaults grantedAuthorityDefaults() {
    return new GrantedAuthorityDefaults(""); // Remove the ROLE_ prefix
  }

  @Bean
  public AccessDeniedHandler accessDeniedHandler(){
    return new QuizbeAccessDeniedHandler();
  }



}
