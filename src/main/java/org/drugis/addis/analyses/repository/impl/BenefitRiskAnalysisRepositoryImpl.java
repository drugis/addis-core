package org.drugis.addis.analyses.repository.impl;

import org.drugis.addis.analyses.model.AnalysisCommand;
import org.drugis.addis.analyses.model.BenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.BenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.analyses.service.BenefitRiskAnalysisService;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.scenarios.repository.ScenarioRepository;
import org.drugis.addis.security.Account;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by daan on 25-2-16.
 */
@Repository
public class BenefitRiskAnalysisRepositoryImpl implements BenefitRiskAnalysisRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  private EntityManager em;

  @Inject
  private AnalysisService analysisService;

  @Inject
  private ProjectService projectService;

  @Inject
  InterventionRepository interventionRepository;

  @Inject
  ScenarioRepository scenarioRepository;

  @Inject
  private BenefitRiskAnalysisService benefitRiskAnalysisService;


  @Override
  public Collection<BenefitRiskAnalysis> queryByProject(Integer projectId) {
    TypedQuery<BenefitRiskAnalysis> query = em.createQuery("FROM BenefitRiskAnalysis " +
            "a WHERE a.projectId = :projectId", BenefitRiskAnalysis.class);
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }

  @Override
  public BenefitRiskAnalysis create(Account user, AnalysisCommand analysisCommand) throws ResourceDoesNotExistException, MethodNotAllowedException, SQLException, IOException {
    projectService.checkProjectExistsAndModifiable(user, analysisCommand.getProjectId());
    BenefitRiskAnalysis benefitRiskAnalysis = new BenefitRiskAnalysis(analysisCommand.getProjectId(), analysisCommand.getTitle());

    em.persist(benefitRiskAnalysis);
    em.flush();

    final Integer metaKey = benefitRiskAnalysis.getId();
    assert (metaKey != null);

    benefitRiskAnalysis.updateIncludedInterventions(new HashSet<>());

    em.flush();
    return benefitRiskAnalysis;
  }

  @Override
  public BenefitRiskAnalysis update(Account user, BenefitRiskAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException {
    benefitRiskAnalysisService.updateBenefitRiskAnalysis(user, analysis);
    BenefitRiskAnalysis oldAnalysis = em.find(BenefitRiskAnalysis.class, analysis.getId());
    analysis.setBenefitRiskNMAOutcomeInclusions(benefitRiskAnalysisService.removeBaselinesWithoutIntervention(analysis, oldAnalysis));
    return em.merge(analysis);
  }

  @Override
  public BenefitRiskAnalysis find(Integer id) {
    return em.find(BenefitRiskAnalysis.class, id);
  }
}
