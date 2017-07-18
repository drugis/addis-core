package org.drugis.addis.config;

import org.drugis.addis.analyses.repository.AnalysisRepository;
import org.drugis.addis.analyses.repository.CriteriaRepository;
import org.drugis.addis.analyses.repository.BenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.effectsTables.repository.EffectsTableRepository;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.models.repository.FunnelPlotRepository;
import org.drugis.addis.models.repository.ModelBaselineRepository;
import org.drugis.addis.models.repository.ModelRepository;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.outcomes.service.OutcomeService;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.problems.service.ProblemService;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.projects.repository.ReportRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.statistics.service.StatisticsService;
import org.drugis.addis.subProblems.repository.SubProblemRepository;
import org.drugis.addis.subProblems.service.SubProblemService;
import org.drugis.addis.trialverse.service.ClinicalTrialsImportService;
import org.drugis.addis.trialverse.service.MappingService;
import org.drugis.addis.trialverse.service.TriplestoreService;
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
        "org.drugis.addis.models.service",
        "org.drugis.addis.statistics.controller",
        "org.drugis.addis.effectsTables.controller"
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
  public ReportRepository mockReportRepository() {
    return mock(ReportRepository.class);
  }

  @Bean
  public ProjectService mockProjectService() {
    return mock(ProjectService.class);
  }

  @Bean
  public InterventionRepository interventionRepository() {
    return mock(InterventionRepository.class);
  }

  @Bean
  public OutcomeRepository outcomeRepository() {
    return mock(OutcomeRepository.class);
  }

  @Bean
  public OutcomeService outcomeService() {
    return mock(OutcomeService.class);
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
  public NetworkMetaAnalysisRepository networkMetaAnalysisRepository() {
    return mock(NetworkMetaAnalysisRepository.class);
  }

  @Bean
  public BenefitRiskAnalysisRepository benefitRiskAnalysisRepository() {
    return mock(BenefitRiskAnalysisRepository.class);
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
  public FunnelPlotRepository mockFunnelPlotRepository() {
    return mock(FunnelPlotRepository.class);
  }

  @Bean
  public PataviTaskRepository mockPataviTaskRepository() {
    return mock(PataviTaskRepository.class);
  }

  @Bean
  public ModelBaselineRepository mockModelBaselineRepository() {
    return mock(ModelBaselineRepository.class);
  }

  @Bean
  public VersionMappingRepository mockVersionMappingRepository() {
    return mock(VersionMappingRepository.class);
  }

  @Bean
  public MappingService mockMappingService() {
    return mock(MappingService.class);
  }

  @Bean
  public InterventionService mockInterventionService() {
    return mock(InterventionService.class);
  }

  @Bean
  public CovariateRepository mockCovariateRepository() {
    return mock(CovariateRepository.class);
  }

  @Bean
  public ClinicalTrialsImportService mockClinicalTrialsImportService() {
    return mock(ClinicalTrialsImportService.class);
  }

  @Bean
  public StatisticsService statisticsService() {
    return mock(StatisticsService.class);
  }

  @Bean
  public EffectsTableRepository effectsTableRepository() {
    return mock(EffectsTableRepository.class);
  }

  @Bean
  public SubProblemService subProblemService() {
    return mock(SubProblemService.class);
  }

  @Bean
  public SubProblemRepository subProblemRepository() {
    return mock(SubProblemRepository.class);
  }
}

