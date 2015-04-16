package org.drugis.addis.config;

import org.drugis.addis.trialverse.factory.RestOperationsFactory;
import org.drugis.addis.trialverse.factory.impl.RestOperationsFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@ComponentScan(excludeFilters = {@ComponentScan.Filter(Configuration.class)}, basePackages = {
        "org.drugis.addis.trialverse.service"
})
public class TrialverseServiceIntegrationTestConfig {
  @Bean
  public RestOperationsFactory restOperationsFactory() {
    return new RestOperationsFactoryImpl();
  }
}

