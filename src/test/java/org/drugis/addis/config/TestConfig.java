package org.drugis.addis.config;

import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.CriteriaRepository;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.remarks.repository.RemarksRepository;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.mock;

@Configuration
@EnableWebMvc
@ComponentScan(excludeFilters = {@ComponentScan.Filter(Configuration.class)}, basePackages = {
        "org.drugis.addis.error",
        "org.drugis.addis.projects.controller",
        "org.drugis.addis.outcomes.controller",
        "org.drugis.addis.interventions.controller",
        "org.drugis.addis.analyses.controller",
        "org.drugis.addis.analyses.service",
        "org.drugis.addis.trialverse.controller",
        "org.drugis.addis.problems.controller",
        "org.drugis.addis.scenarios.controller",
        "org.drugis.addis.scenarios.service",
        "org.drugis.addis.models.controller",
        "org.drugis.addis.models.service"
})
public class TestConfig {
  @Bean
  public AccountRepository mockAccountRepository() {
    return mock(AccountRepository.class);
  }

  @Bean
  public ProjectRepository mockProjectsRepository() {
    return mock(ProjectRepository.class);
  }

  @Bean
  public ProjectService mockProjectService() { return mock(ProjectService.class); }

  @Bean
  public InterventionRepository interventionRepository() {
    return mock(InterventionRepository.class);
  }

  @Bean
  public OutcomeRepository outcomeRepository() {
    return mock(OutcomeRepository.class);
  }

  @Bean
  public AnalysisService analysisService() {
    return mock(AnalysisService.class);
  }

  @Bean
  public AnalysisRepository analysisRepository() {
    return mock(AnalysisRepository.class);
  }

  @Bean
  public SingleStudyBenefitRiskAnalysisRepository singleStudyBenefitRiskAnalysisRepository() {
    return mock(SingleStudyBenefitRiskAnalysisRepository.class);
  }

  @Bean
  public NetworkMetaAnalysisRepository networkMetaAnalysisRepository() {
    return mock(NetworkMetaAnalysisRepository.class);
  }

  @Bean
  public CriteriaRepository criteriaRepository() {
    return mock(CriteriaRepository.class);
  }

  @Bean
  public TriplestoreService mockTriplestoreService() {
    return mock(TriplestoreService.class);
  }

  @Bean
  public ProblemService mockProblemService() {
    return mock(ProblemService.class);
  }

  @Bean
  public ScenarioRepository mockScenarioRepository() {
    return mock(ScenarioRepository.class);
  }

  @Bean
  public ModelRepository mockModelRepository() {
    return mock(ModelRepository.class);
  }

  @Bean
  public PataviTaskRepository mockPataviTaskRepository() {
    return mock(PataviTaskRepository.class);
  }

  @Bean
  public RemarksRepository mockRemarksRepository() { return mock(RemarksRepository.class);}

  @Bean
  public VersionMappingRepository mockVersionMappingRepository() {
    return mock(VersionMappingRepository.class);
  }

}

