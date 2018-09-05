/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.drugis.addis.config;

import org.apache.jena.ext.com.google.common.base.Optional;
import org.drugis.addis.security.ApplicationKeyAuthenticationProvider;
import org.drugis.addis.security.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.social.UserIdSource;
import org.springframework.social.security.AuthenticationNameUserIdSource;
import org.springframework.social.security.SpringSocialConfigurer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private ApplicationContext context;

  @Inject
  @Qualifier("dsAddisCore")
  private DataSource dataSource;

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.jdbcAuthentication()
            .dataSource(dataSource)
            .usersByUsernameQuery("SELECT username, password, TRUE FROM Account WHERE username = ?")
            .authoritiesByUsernameQuery("SELECT Account.username, COALESCE(AccountRoles.role, 'ROLE_USER') FROM Account" +
                    " LEFT OUTER JOIN AccountRoles ON Account.id = AccountRoles.accountId WHERE Account.username = ?")
            .passwordEncoder(passwordEncoder());
    auth.authenticationProvider(tokenAuthenticationProvider());

  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    web
            .ignoring()
            .antMatchers("/resources/**");
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    String[] whitelist = {
            "/",
            "/trialverse",
            "/trialverse/**",
            "/patavi", // allow POST mcda models anonymously
            "/statistics/**", // allow calculation of estimations for d80 table
            "/favicon.ico", "/favicon.png", "/app/**", "/auth/**",
            "/signin", "/signup", "/**/modal/*.html", "/manual.html"};
    // Disable CSFR protection on the following urls:
    List<AntPathRequestMatcher> requestMatchers = Arrays.stream(whitelist)
            .map(AntPathRequestMatcher::new)
            .collect(Collectors.toList());
    CookieCsrfTokenRepository csrfTokenRepository = new CookieCsrfTokenRepository();
    csrfTokenRepository.setCookieHttpOnly(false);
    http
            .formLogin()
            .loginPage("/signin")
            .loginProcessingUrl("/signin/authenticate")
            .failureUrl("/signin?param.error=bad_credentials")
            .and().authorizeRequests()
            .antMatchers(whitelist).permitAll()
            .antMatchers(HttpMethod.GET, "/**").permitAll()
            .antMatchers(HttpMethod.POST, "/**").authenticated()
            .antMatchers(HttpMethod.PUT, "/**").authenticated()
            .antMatchers(HttpMethod.DELETE, "/**").authenticated()
            .and().rememberMe()
            .and().exceptionHandling()
            .authenticationEntryPoint(new Http403ForbiddenEntryPoint())
            .and().apply(
            new SpringSocialConfigurer()
                    .alwaysUsePostLoginUrl(false)
    )
            .and().csrf()
            .csrfTokenRepository(csrfTokenRepository)
            .requireCsrfProtectionMatcher(request ->
                    !(requestMatchers.stream().anyMatch(matcher -> matcher.matches(request))
                            || Optional.fromNullable(request.getHeader("X-Auth-Application-Key")).isPresent()
                            || HttpMethod.GET.toString().equals(request.getMethod())))
            .and().setSharedObject(ApplicationContext.class, context);

    http.addFilterBefore(new AuthenticationFilter(authenticationManager()), BasicAuthenticationFilter.class);

  }

  @Bean
  public UserDetailsService userDetailsService() {
    return super.userDetailsService();
  }

  @Bean
  public UserIdSource userIdSource() {
    return new AuthenticationNameUserIdSource();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider tokenAuthenticationProvider() {
    return new ApplicationKeyAuthenticationProvider();
  }

  @Bean
  public AuthenticationEntryPoint unauthorizedEntryPoint() {
    return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }


}
