package org.quizbe.config

import org.quizbe.service.CustomUserServiceDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.core.GrantedAuthorityDefaults
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.util.matcher.AntPathRequestMatcher


@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class WebSecurityConfiguration @Autowired constructor(private val userDetailsService: CustomUserServiceDetails) {

    @Bean
    @Throws(java.lang.Exception::class)
    fun authManager(
        http: HttpSecurity,
        bCryptPasswordEncoder: BCryptPasswordEncoder?,
        userDetailService: CustomUserServiceDetails?
    ): AuthenticationManager? {
        return http.getSharedObject(AuthenticationManagerBuilder::class.java)
            .userDetailsService(userDetailsService)
            .passwordEncoder(bCryptPasswordEncoder)
            .and()
            .build()
    }

    @Bean
    @Throws(java.lang.Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain? {
        http.authorizeHttpRequests()
            .antMatchers("/").permitAll()
            .antMatchers("/login").permitAll()
            .antMatchers("/access-denied").permitAll()
            .antMatchers("/register").permitAll()
            .antMatchers("/webjars/**").permitAll()
            .antMatchers("/error").permitAll() //.antMatchers("/admin/**").permitAll()
            .antMatchers("/question/**").hasAnyAuthority("USER")
            .antMatchers("/user/**").hasAuthority("USER")
            .antMatchers("/douser/**")
            .hasAuthority("CHANGE_PW") //            .antMatchers("/topic/**").hasAuthority("TEACHER")
            .antMatchers("/admin/**").hasAuthority("ADMIN").anyRequest()
            .authenticated()
            .and() //.csrf().disable()
            .formLogin()
            .loginPage("/login").failureUrl("/login?error=true")
            .defaultSuccessUrl("/question?login")
            .usernameParameter("username")
            .passwordParameter("password")
            .and()
            .logout()
            .logoutRequestMatcher(AntPathRequestMatcher("/logout"))
            .logoutSuccessUrl("/login")
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID")
            .and()
            .exceptionHandling().accessDeniedHandler(accessDeniedHandler())
        // .exceptionHandling().accessDeniedPage("/access-denied");
        return http.build()
    }

    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer? {
        return WebSecurityCustomizer { web: WebSecurity ->
            web
                .ignoring()
                .antMatchers(
                    "/resources/**",
                    "/svg/**",
                    "/images/**",
                    "/static/**",
                    "/css/**",
                    "/js/**",
                    "/images/**, /console/**" +
                            ""
                )
        }
    }

    @Bean
    fun bCryptPasswordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun grantedAuthorityDefaults(): GrantedAuthorityDefaults {
        return GrantedAuthorityDefaults("") // Remove the ROLE_ prefix
    }

    @Bean
    fun accessDeniedHandler(): AccessDeniedHandler {
        return QuizbeAccessDeniedHandler()
    }
}