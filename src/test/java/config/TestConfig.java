package config;

import org.drugis.trialverse.security.repository.AccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.mock;

@Configuration
@EnableWebMvc
@ComponentScan(excludeFilters = {@ComponentScan.Filter(Configuration.class)}, basePackages = {"org.drugis.trialverse"})
public class TestConfig {
  @Bean
  public AccountRepository mockAccountRepository() {
    return mock(AccountRepository.class);
  }

}

