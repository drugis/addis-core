package org.drugis.addis.testConfig;

import org.drugis.addis.security.repository.AccountRepository;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"org.drugis.addis.config", "org.drugis.addis.error"}, excludeFilters = { @ComponentScan.Filter(Configuration.class) })
public class TestConfig {
    @Bean
    public AccountRepository mockAccountRepository() {
        return Mockito.mock(AccountRepository.class);
    }
}

