package org.drugis.trialverse.config;

import org.apache.jena.ext.com.google.common.base.Optional;
import org.drugis.trialverse.security.ApplicationKeyAuthenticationProvider;
import org.drugis.trialverse.security.AuthenticationFilter;
import org.drugis.trialverse.security.SimpleSocialUsersDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.social.UserIdSource;
import org.springframework.social.security.AuthenticationNameUserIdSource;
import org.springframework.social.security.SocialUserDetailsService;
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
    String[] whitelist = {"/", "/favicon.ico", "/favicon.png", "/app/**", "/auth/**", "/signin", "/signup", "/**/modal/*.html"};
    // Disable CSFR protection on the following urls:
    List<AntPathRequestMatcher> requestMatchers = Arrays.asList(whitelist)
            .stream()
            .map(AntPathRequestMatcher::new)
            .collect(Collectors.toList());
    http
            .formLogin()
            .loginPage("/signin")
            .loginProcessingUrl("/signin/authenticate")
            .failureUrl("/signin?param.error=bad_credentials")
            .defaultSuccessUrl("/")
            .and().logout()
            .logoutUrl("/signout")
            .deleteCookies("JSESSIONID")
            .and().authorizeRequests()
            .antMatchers(whitelist).permitAll()
            .antMatchers("/monitoring").hasRole("MONITORING")
            .antMatchers("/**").authenticated()
            .and().rememberMe()
            .and().exceptionHandling()
            .authenticationEntryPoint(new Http403ForbiddenEntryPoint())
            .and().apply(
            new SpringSocialConfigurer()
                    .postLoginUrl("/")
                    .alwaysUsePostLoginUrl(true))
            .and().csrf().requireCsrfProtectionMatcher(request ->
            !(requestMatchers.stream().anyMatch(matcher -> matcher.matches(request))
                    || Optional.fromNullable(request.getHeader("X-Auth-Application-Key")).isPresent()))
            .and().setSharedObject(ApplicationContext.class, context)
    ;

    http.addFilterBefore(new AuthenticationFilter(authenticationManager()), BasicAuthenticationFilter.class);

  }

  @Bean
  public AuthenticationProvider tokenAuthenticationProvider() {
    return new ApplicationKeyAuthenticationProvider();
  }

  @Bean
  public AuthenticationEntryPoint unauthorizedEntryPoint() {
    return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Bean
  public SocialUserDetailsService socialUsersDetailService() {
    return new SimpleSocialUsersDetailService(userDetailsService());
  }

  @Bean
  public UserIdSource userIdSource() {
    return new AuthenticationNameUserIdSource();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


}
