package org.drugis.trialverse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.Properties;

import static org.mockito.Mockito.mock;

/**
 * Created by connor on 6-11-14.
 */
@Configuration
@ComponentScan(excludeFilters = {@ComponentScan.Filter(Configuration.class)},
        basePackages = {"org.drugis.trialverse.security"})
@EnableJpaRepositories(basePackages = {})
public class JpaRepositoryTestConfig {

  @Bean
  public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
    return new PersistenceExceptionTranslationPostProcessor();
  }

  @Bean
  public ConnectionFactoryLocator connectionFactoryLocator() {
    return mock(ConnectionFactoryLocator.class);
  }

  @Bean
  public UsersConnectionRepository usersConnectionRepository() {
    return mock(UsersConnectionRepository.class);
  }

  @Bean
  public UserDetailsService userDetailsService() {
    return mock(UserDetailsService.class);
  }
}
