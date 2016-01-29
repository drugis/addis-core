package org.drugis.addis.config;

import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.security.repository.ApiKeyRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.security.SocialAuthenticationServiceLocator;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.sql.DataSource;

import static org.mockito.Mockito.mock;

/**
 * Created by daan on 16-9-15.
 */
@Configuration
@EnableWebMvc
@ComponentScan(excludeFilters = {@ComponentScan.Filter(Configuration.class)},
        basePackages = {"org.drugis.addis.security"})
public class SecurityConfigTestConfig {
  @Bean
  public UsersConnectionRepository usersConnectionRepository() {
    return mock(UsersConnectionRepository.class);
  }

  @Bean
  public SocialAuthenticationServiceLocator socialAuthenticationServiceLocator() {
    return mock(SocialAuthenticationServiceLocator.class);
  }

  @Bean(name="dsAddisCore")
  public DataSource dataSource() {
    return mock(DataSource.class);
  }

  @Bean(name = "jtAddisCore")
  public JdbcTemplate jdbcTemplate() {
    return mock(JdbcTemplate.class);
  }

  @Bean
  public AccountRepository accountRepository() {
    return mock(AccountRepository.class);
  }

  @Bean
  public ApiKeyRepository apiKeyRepository() {
    return mock(ApiKeyRepository.class);
  }
}
