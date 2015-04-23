package org.drugis.addis.config;

import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.problems.service.impl.PerformanceTableBuilder;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.trialverse.factory.RestOperationsFactory;
import org.drugis.addis.trialverse.factory.impl.RestOperationsFactoryImpl;
import org.drugis.addis.trialverse.service.TrialverseService;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.inject.Inject;

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

