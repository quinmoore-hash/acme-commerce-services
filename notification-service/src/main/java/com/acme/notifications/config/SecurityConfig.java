package com.acme.notifications.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("order-service").password("{noop}order-service").roles("PUBLISHER")
                .and()
                .withUser("support").password("{noop}support").roles("PUBLISHER", "SUPPORT");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .authorizeRequests()
                .mvcMatchers("/actuator/health", "/actuator/info").permitAll()
                .mvcMatchers("/actuator/**").hasRole("SUPPORT")
                .antMatchers("/api/notifications/**").hasAnyRole("PUBLISHER", "SUPPORT")
                .anyRequest().denyAll()
                .and()
                .httpBasic();
    }
}
