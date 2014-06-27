package org.drugis.addis.config;

import org.drugis.addis.trialverse.factory.RestOperationsFactory;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@ComponentScan(excludeFilters = {@ComponentScan.Filter(Configuration.class)}, basePackages = {
        "org.drugis.addis.trialverse.service"
})
public class TrialverseServiceTestConfig {
  @Bean
  public TrialverseRepository mockTrialverseRepository() {
    return mock(TrialverseRepository.class);
  }

  @Bean
  public RestOperationsFactory mockRestOperationsFactory() {
    return mock(RestOperationsFactory.class);
  }
}

