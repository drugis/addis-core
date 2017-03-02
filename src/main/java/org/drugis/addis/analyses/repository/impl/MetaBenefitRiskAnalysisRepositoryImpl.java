package org.drugis.addis.analyses.repository.impl;

import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.InterventionInclusion;
import org.drugis.addis.analyses.MbrOutcomeInclusion;
import org.drugis.addis.analyses.MetaBenefitRiskAnalysis;
import org.drugis.addis.analyses.repository.MetaBenefitRiskAnalysisRepository;
import org.drugis.addis.analyses.service.AnalysisService;
import org.drugis.addis.analyses.service.MetaBenefitRiskAnalysisService;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by daan on 25-2-16.
 */
@Repository
public class MetaBenefitRiskAnalysisRepositoryImpl implements MetaBenefitRiskAnalysisRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Inject
  AnalysisService analysisService;

  @Inject
  ProjectService projectService;

  @Inject
  InterventionRepository interventionRepository;

  @Inject
  ScenarioRepository scenarioRepository;

  @Inject
  MetaBenefitRiskAnalysisService metaBenefitRiskAnalysisService;


  @Override
  public Collection<MetaBenefitRiskAnalysis> queryByProject(Integer projectId) {
    TypedQuery<MetaBenefitRiskAnalysis> query = em.createQuery("FROM MetaBenefitRiskAnalysis " +
            "a WHERE a.projectId = :projectId", MetaBenefitRiskAnalysis.class);
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }

  @Override
  public MetaBenefitRiskAnalysis create(Account user, AnalysisCommand analysisCommand) throws ResourceDoesNotExistException, MethodNotAllowedException, SQLException, IOException {
    projectService.checkProjectExistsAndModifiable(user, analysisCommand.getProjectId());
    MetaBenefitRiskAnalysis metaBenefitRiskAnalysis = new MetaBenefitRiskAnalysis(analysisCommand.getProjectId(), analysisCommand.getTitle());

    em.persist(metaBenefitRiskAnalysis);
    em.flush();

    final Integer metaKey = metaBenefitRiskAnalysis.getId();
    assert (metaKey != null);

    Collection<AbstractIntervention> interventions = interventionRepository.query(metaBenefitRiskAnalysis.getProjectId());
    List<InterventionInclusion> interventionInclusions = interventions.stream().map(i -> new InterventionInclusion(metaKey, i.getId())).collect(Collectors.toList());
    interventionInclusions.forEach(ii -> em.persist(ii));
    metaBenefitRiskAnalysis.updateIncludedInterventions(new HashSet<>(interventionInclusions));

    em.flush();

    List<MbrOutcomeInclusion> outcomeInclusions = analysisService.buildInitialOutcomeInclusions(analysisCommand.getProjectId(), metaBenefitRiskAnalysis.getId());
    metaBenefitRiskAnalysis.setMbrOutcomeInclusions(outcomeInclusions);

    return metaBenefitRiskAnalysis;
  }

  @Override
  public MetaBenefitRiskAnalysis update(Account user, MetaBenefitRiskAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException {
    metaBenefitRiskAnalysisService.checkMetaBenefitRiskAnalysis(user, analysis);
    MetaBenefitRiskAnalysis oldAnalysis = em.find(MetaBenefitRiskAnalysis.class, analysis.getId());
    analysis.setMbrOutcomeInclusions(metaBenefitRiskAnalysisService.cleanInclusions(analysis, oldAnalysis));
    return em.merge(analysis);
  }

  @Override
  public MetaBenefitRiskAnalysis find(Integer id) {
    return em.find(MetaBenefitRiskAnalysis.class, id);
  }
}
