package org.drugis.addis.projects.service;

import com.google.common.collect.Sets;
import org.drugis.addis.covariates.Covariate;
import org.drugis.addis.covariates.CovariateRepository;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.controller.command.*;
import org.drugis.addis.interventions.model.*;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.interventions.service.InterventionService;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.outcomes.repository.OutcomeRepository;
import org.drugis.addis.projects.Project;
import org.drugis.addis.projects.ProjectCommand;
import org.drugis.addis.projects.repository.ProjectRepository;
import org.drugis.addis.projects.service.impl.ProjectServiceImpl;
import org.drugis.addis.projects.service.impl.UpdateProjectException;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.addis.trialverse.model.SemanticInterventionUriAndName;
import org.drugis.addis.trialverse.model.SemanticVariable;
import org.drugis.addis.trialverse.model.emun.CovariateOption;
import org.drugis.addis.trialverse.model.emun.CovariateOptionType;
import org.drugis.addis.trialverse.service.TriplestoreService;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by connor on 16-4-14.
 */
public class ProjectServiceTest {

  @Mock
  private AccountRepository accountRepository;

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private VersionMappingRepository versionMappingRepository;

  @Mock
  private OutcomeRepository outcomeRepository;

  @Mock
  private CovariateRepository covariateRepository;

  @Mock
  private InterventionRepository interventionRepository;

  @Mock
  private TriplestoreService triplestoreService;

  @InjectMocks
  private
  ProjectService projectService;

  private Integer projectId = 1;
  private String username = "gert";
  private Principal principal = mock(Principal.class);
  private Account account = mock(Account.class);
  private Project mockProject = mock(Project.class);
  private VersionMapping mapping = mock(VersionMapping.class);

  private static final String datasetUuid = "datasetUuid";
  private static final URI versionedDatasetUri = URI.create("http://trials.drugis.org/datasets/" + datasetUuid);

  @Before
  public void setUp() throws ResourceDoesNotExistException, URISyntaxException {

    projectService = new ProjectServiceImpl();

    initMocks(this);

    when(mockProject.getOwner()).thenReturn(account);
    when(mockProject.getNamespaceUid()).thenReturn(datasetUuid);
    when(principal.getName()).thenReturn(username);
    when(projectRepository.get(projectId)).thenReturn(mockProject);
    when(accountRepository.findAccountByUsername(username)).thenReturn(account);
    when(versionMappingRepository.getVersionMappingByDatasetUrl(versionedDatasetUri)).thenReturn(mapping);
    when(mapping.getVersionedDatasetUri()).thenReturn(versionedDatasetUri);
  }

  @Test
  public void testCheckOwnership() throws Exception {
    projectService.checkOwnership(projectId, principal);
  }

  @Test(expected = MethodNotAllowedException.class)
  public void testOwnershipFails() throws MethodNotAllowedException, ResourceDoesNotExistException {
    Account account2 = mock(Account.class);

    when(mockProject.getOwner()).thenReturn(account2);

    projectService.checkOwnership(projectId, principal);
  }

  @Test
  public void update() throws ResourceDoesNotExistException, UpdateProjectException {
    String name = "name";
    String description = "description";
    when(projectRepository.isExistingProjectName(projectId, name)).thenReturn(Boolean.FALSE);
    when(projectRepository.updateNameAndDescription(projectId, name, description)).thenReturn(mockProject);
    Project project = projectService.updateProject(projectId, name, description);
    assertEquals(mockProject, project);
    verify(projectRepository).isExistingProjectName(projectId, name);
    verify(projectRepository).updateNameAndDescription(projectId, name, description);
  }

  @Test(expected = UpdateProjectException.class)
  public void updateDuplicateName() throws ResourceDoesNotExistException, UpdateProjectException {
    String name = "name";
    String description = "description";
    when(projectRepository.isExistingProjectName(projectId, name)).thenReturn(Boolean.TRUE);
    projectService.updateProject(projectId, name, description);
  }

  @Test
  public void copy() throws Exception, ReadValueException {
    Integer newProjectId = -3;
    URI headVersion = URI.create("http://www.drugis.org/datasets/headversion");
    ProjectCommand mockCommand = mock(ProjectCommand.class);
    Project mockNewProject = mock(Project.class);
    when(mockNewProject.getId()).thenReturn(newProjectId);
    when(mockProject.getCommand()).thenReturn(mockCommand);
    when(triplestoreService.getHeadVersion((mapping.getVersionedDatasetUri()))).thenReturn(headVersion.toString());
    when(projectRepository.create(account, mockCommand)).thenReturn(mockNewProject);

    // outcomes
    Integer filteredInOutcomeId = 1;
    Integer filteredOutOutcomeId = 2;
    SemanticVariable semanticOutcomeFilteredIn = new SemanticVariable(URI.create("http://www.bs.org/outcomeIn"), "semanticLabel1");
    SemanticVariable semanticOutcomeFilteredOut = new SemanticVariable(URI.create("http://www.bs.org/outcomeOut"), "semanticLabel2");
    Outcome outcomeFilteredIn = new Outcome(filteredInOutcomeId, projectId, "outcome1", "motivation", semanticOutcomeFilteredIn);
    Outcome outcomeFilteredOut = new Outcome(filteredOutOutcomeId, projectId, "outcome2", "motivation", semanticOutcomeFilteredOut);
    Collection<Outcome> sourceOutcomes = Arrays.asList(outcomeFilteredIn, outcomeFilteredOut);
    SemanticVariable semanticOutcome = new SemanticVariable(semanticOutcomeFilteredIn.getUri(), semanticOutcomeFilteredIn.getLabel());
    List<SemanticVariable> semanticOutcomes = Collections.singletonList(semanticOutcome);
    Outcome newOutcome = mock(Outcome.class);
    when(triplestoreService.getOutcomes(datasetUuid, headVersion)).thenReturn(semanticOutcomes);
    when(outcomeRepository.query(projectId)).thenReturn(sourceOutcomes);
    when(outcomeRepository.create(account, newProjectId, outcomeFilteredIn.getName(), outcomeFilteredIn.getDirection(), outcomeFilteredIn.getMotivation(),
            outcomeFilteredIn.getSemanticVariable())).thenReturn(newOutcome);

    // covariates
    URI covariateFilteredInUri = URI.create("http://trials.drugis.org/concepts/age");
    Integer covariateInId = -10;
    Covariate covariateFilteredIn = new Covariate(covariateInId, newProjectId, "covariate in", null, covariateFilteredInUri.toString(),
            CovariateOptionType.POPULATION_CHARACTERISTIC);
    Covariate covariateFilteredOut = new Covariate(-11, newProjectId, "covariate out", null, "http://nothere.com",
            CovariateOptionType.POPULATION_CHARACTERISTIC);
    Covariate covariateStudyLevel = new Covariate(-13, newProjectId, "covariate study level", null,
            CovariateOption.LENGTH_OF_FOLLOW_UP.toString(), CovariateOptionType.STUDY_CHARACTERISTIC);
    Collection<Covariate> covariates = Arrays.asList(covariateFilteredIn, covariateFilteredOut, covariateStudyLevel);
    SemanticVariable semanticCovariateFilteredIn = new SemanticVariable(covariateFilteredInUri, "bla");
    when(covariateRepository.findByProject(projectId)).thenReturn(covariates);
    List<SemanticVariable> populationCharacteristics = Collections.singletonList(semanticCovariateFilteredIn);
    when(triplestoreService.getPopulationCharacteristics(datasetUuid, headVersion)).thenReturn(populationCharacteristics);

    // Interventions
    // Cases:
    // - simple intervention, semantic concept still in dataset
    // - simple intervention, semantic concept no longer in dataset
    // - fixed dose intervention, units and concept still in dataset
    // - fixed dose intervention, units no longer in dataset
    // - titrated dose intervention, units and concept still in dataset
    // - titrated dose intervention, units no longer in dataset
    // - bothtypes intervention, units and concept still in dataset
    // - bothtypes intervention, units no longer in dataset
    // - combination intervention, component interventions successfully copied
    // - combination intervention, component interventions not successfully copied
    // - intervention set, component interventions (combination intervention among them) successfully copied
    // - intervention set, component interventions (combination intervention among them) not successfully copied
    URI semanticInterventionUri = URI.create("http://bla.com/semanticInterventions/1");
    SimpleIntervention simpleInterventionFilteredIn = new SimpleIntervention(1, projectId, "simple in", null,
            semanticInterventionUri, "semantic intervention label");
    SimpleIntervention simpleInterventionFilteredOut = new SimpleIntervention(2, projectId, "simple out", null,
            URI.create("http://bla.com/semanticInterventions/2"), "semantic intervention label");
    URI gramUri = URI.create("http://trials.drugis.org/concepts/gram");
    LowerBoundCommand lowerBoundFilteredIn = new LowerBoundCommand(LowerBoundType.AT_LEAST, 0.1, "mg", "pt1d",
            gramUri);
    LowerBoundCommand lowerBoundFilteredOut = new LowerBoundCommand(LowerBoundType.AT_LEAST, 0.1, "mg", "pt1d",
            URI.create("http://nonsense.com"));
    UpperBoundCommand upperBound = null;
    DoseConstraint constraintFilteredIn = new DoseConstraint(lowerBoundFilteredIn, upperBound);
    DoseConstraint constraintFilteredOut = new DoseConstraint(lowerBoundFilteredOut, upperBound);
    FixedDoseIntervention fixedDoseInterventionFilteredIn = new FixedDoseIntervention(3, projectId, "fixed dose in", null,
            semanticInterventionUri, "semantic intervention label", constraintFilteredIn);
    FixedDoseIntervention fixedDoseInterventionFilteredOut = new FixedDoseIntervention(4, projectId, "fixed dose out", null,
            semanticInterventionUri, "semantic intervention label", constraintFilteredOut);
    TitratedDoseIntervention titratedDoseInterventionFilteredIn = new TitratedDoseIntervention(5, projectId, "titrated dose in", null,
            semanticInterventionUri, "semantic intervention label", constraintFilteredIn, null);
    TitratedDoseIntervention titratedDoseInterventionFilteredOut = new TitratedDoseIntervention(6, projectId, "titrated dose out", null,
            semanticInterventionUri, "semantic intervention label", null, constraintFilteredOut);
    BothDoseTypesIntervention bothDoseTypesInterventionFilteredIn = new BothDoseTypesIntervention(7, projectId, "both dose in", null,
            semanticInterventionUri, "semantic intervention label", null, constraintFilteredIn);
    BothDoseTypesIntervention bothDoseTypesInterventionFilteredOut = new BothDoseTypesIntervention(8, projectId, "both dose out", null,
            semanticInterventionUri, "semantic intervention label", null, constraintFilteredOut);
    CombinationIntervention combinationInterventionFilteredIn = new CombinationIntervention(9, projectId, "combo in", null,
            Sets.newHashSet(simpleInterventionFilteredIn.getId(), fixedDoseInterventionFilteredIn.getId()));
    CombinationIntervention combinationInterventionFilteredOut = new CombinationIntervention(10, projectId, "combo out", null,
            Sets.newHashSet(simpleInterventionFilteredIn.getId(), titratedDoseInterventionFilteredOut.getId()));
    InterventionSet interventionSetFilteredIn = new InterventionSet(11, projectId, "set in", null,
            Sets.newHashSet(combinationInterventionFilteredIn.getId(), titratedDoseInterventionFilteredIn.getId()));
    InterventionSet interventionSetFilteredOut = new InterventionSet(12, projectId, "set out", null,
            Sets.newHashSet(combinationInterventionFilteredOut.getId(), titratedDoseInterventionFilteredIn.getId()));

    Set<AbstractIntervention> sourceInterventions = Sets.newHashSet(simpleInterventionFilteredIn, simpleInterventionFilteredOut,
            fixedDoseInterventionFilteredIn, fixedDoseInterventionFilteredOut, titratedDoseInterventionFilteredIn, titratedDoseInterventionFilteredOut,
            bothDoseTypesInterventionFilteredIn, bothDoseTypesInterventionFilteredOut, combinationInterventionFilteredIn,
            combinationInterventionFilteredOut, interventionSetFilteredIn, interventionSetFilteredOut);
    SemanticInterventionUriAndName semanticIntervention = new SemanticInterventionUriAndName(semanticInterventionUri, "something");
    List<SemanticInterventionUriAndName> semanticInterventions = Collections.singletonList(semanticIntervention);
    when(interventionRepository.query(projectId)).thenReturn(sourceInterventions);
    when(triplestoreService.getInterventions(datasetUuid, headVersion)).thenReturn(semanticInterventions);
    List<URI> unitUris = Collections.singletonList(gramUri);
    when(triplestoreService.getUnitUris(datasetUuid, headVersion)).thenReturn(unitUris);

    AbstractInterventionCommand simpleCommand = InterventionService.buildSingleInterventionCommand(newProjectId, simpleInterventionFilteredIn);
    AbstractInterventionCommand fixedCommand = InterventionService.buildSingleInterventionCommand(newProjectId, fixedDoseInterventionFilteredIn);
    AbstractInterventionCommand titratedCommand = InterventionService.buildSingleInterventionCommand(newProjectId, titratedDoseInterventionFilteredIn);
    AbstractInterventionCommand bothTypesCommand = InterventionService.buildSingleInterventionCommand(newProjectId, bothDoseTypesInterventionFilteredIn);
    mockCreateSingleIntervention(simpleInterventionFilteredIn, SimpleIntervention.class, simpleCommand);
    mockCreateSingleIntervention(fixedDoseInterventionFilteredIn, FixedDoseIntervention.class, fixedCommand);
    mockCreateSingleIntervention(titratedDoseInterventionFilteredIn, TitratedDoseIntervention.class, titratedCommand);
    mockCreateSingleIntervention(bothDoseTypesInterventionFilteredIn, BothDoseTypesIntervention.class, bothTypesCommand);
    AbstractInterventionCommand combiCommand = new CombinationInterventionCommand(newProjectId, combinationInterventionFilteredIn.getName(),
            combinationInterventionFilteredIn.getMotivation(), Sets.newHashSet(-simpleInterventionFilteredIn.getId(), -fixedDoseInterventionFilteredIn.getId()));
    CombinationIntervention newCombination = mock(CombinationIntervention.class);
    when(newCombination.getId()).thenReturn(-combinationInterventionFilteredIn.getId());
    when(interventionRepository.create(account, combiCommand)).thenReturn(newCombination);
    AbstractInterventionCommand setCommand = new InterventionSetCommand(newProjectId, interventionSetFilteredIn.getName(),
            interventionSetFilteredIn.getMotivation(), Sets.newHashSet(-combinationInterventionFilteredIn.getId(), -titratedDoseInterventionFilteredIn.getId()));
    InterventionSet newSet = mock(InterventionSet.class);
    when(newSet.getId()).thenReturn(-combinationInterventionFilteredIn.getId());
    when(interventionRepository.create(account, setCommand)).thenReturn(newSet);


    /// *8888888888**************************** GO *********************888888888888888* ///
    projectService.copy(account, projectId);
    /// *8888888888**************************** GO *********************888888888888888* ///

    verify(outcomeRepository).query(projectId);
    verify(outcomeRepository).create(account, newProjectId, outcomeFilteredIn.getName(), outcomeFilteredIn.getDirection(), outcomeFilteredIn.getMotivation(),
            outcomeFilteredIn.getSemanticVariable());
    verifyNoMoreInteractions(outcomeRepository);

    verify(covariateRepository).findByProject(projectId);
    verify(covariateRepository).createForProject(newProjectId, covariateFilteredInUri.toString(), covariateFilteredIn.getName(),
            covariateFilteredIn.getMotivation(), CovariateOptionType.POPULATION_CHARACTERISTIC);
    verify(covariateRepository).createForProject(newProjectId, covariateStudyLevel.getDefinitionKey(), covariateStudyLevel.getName(),
            covariateStudyLevel.getMotivation(), CovariateOptionType.STUDY_CHARACTERISTIC);
    verifyNoMoreInteractions(covariateRepository);

    verify(interventionRepository).query(projectId);
    verify(interventionRepository).create(account, simpleCommand);
    verify(interventionRepository).create(account, fixedCommand);
    verify(interventionRepository).create(account, titratedCommand);
    verify(interventionRepository).create(account, bothTypesCommand);
    verify(interventionRepository).create(account, combiCommand);
    verify(interventionRepository).create(account, setCommand);
    verifyNoMoreInteractions(interventionRepository);

    verify(triplestoreService).getHeadVersion(mapping.getVersionedDatasetUri());
    verify(triplestoreService).getOutcomes(datasetUuid, headVersion);
    verify(triplestoreService).getPopulationCharacteristics(datasetUuid, headVersion);
    verify(triplestoreService).getUnitUris(datasetUuid, headVersion);
    verify(triplestoreService).getInterventions(datasetUuid, headVersion);
    verifyNoMoreInteractions(triplestoreService);
  }

  private void mockCreateSingleIntervention(SingleIntervention intervention, Class<? extends SingleIntervention> classToMock, AbstractInterventionCommand command) throws InvalidConstraintException, MethodNotAllowedException, ResourceDoesNotExistException {
    AbstractIntervention newFixedIn = mock(classToMock);
    when(newFixedIn.getId()).thenReturn(-intervention.getId());
    when(interventionRepository.create(account, command)).thenReturn(newFixedIn);
  }
}
