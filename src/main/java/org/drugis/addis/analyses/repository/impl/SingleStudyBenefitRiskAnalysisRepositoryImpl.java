package org.drugis.addis.analyses.repository.impl;

import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.InterventionInclusion;
import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.repository.InterventionRepository;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.projects.service.ProjectService;
import org.drugis.addis.security.Account;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * Created by connor on 3/11/14.
 */
@Repository
public class SingleStudyBenefitRiskAnalysisRepositoryImpl implements org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Inject
  InterventionRepository interventionRepository;

  @Inject
  private ProjectService projectService;

  @Override
  public Collection<SingleStudyBenefitRiskAnalysis> query(Integer projectId) {
    TypedQuery<SingleStudyBenefitRiskAnalysis> query = em.createQuery("FROM SingleStudyBenefitRiskAnalysis " +
            "a WHERE a.projectId = :projectId", SingleStudyBenefitRiskAnalysis.class);
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }

  @Override
  public SingleStudyBenefitRiskAnalysis create(AnalysisCommand analysisCommand) throws MethodNotAllowedException, ResourceDoesNotExistException {
    SingleStudyBenefitRiskAnalysis newAnalysis = new SingleStudyBenefitRiskAnalysis(analysisCommand.getProjectId(), analysisCommand.getTitle(), Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    em.persist(newAnalysis);
    return newAnalysis;
  }

  @Override
  public SingleStudyBenefitRiskAnalysis update(Account user, SingleStudyBenefitRiskAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException {
    projectService.checkProjectExistsAndModifiable(user, analysis.getProjectId());

    if (isNotEmpty(analysis.getSelectedOutcomes())) {
      // do not allow selection of outcomes that are not in the project
      for (Outcome outcome : analysis.getSelectedOutcomes()) {
        if (!outcome.getProject().equals(analysis.getProjectId())) {
          throw new ResourceDoesNotExistException();
        }
      }
    }

    List<AbstractIntervention> interventions = interventionRepository.query(analysis.getProjectId());
    Map<Integer, AbstractIntervention> interventionMap = interventions
            .stream().collect(Collectors.toMap(AbstractIntervention::getId, Function.identity()));
    if (isNotEmpty(analysis.getInterventionInclusions())) {
      // do not allow selection of interventions that are not in the project
      for (InterventionInclusion intervention : analysis.getInterventionInclusions()) {
        if (interventionMap.get(intervention.getInterventionId()) == null) {
          throw new ResourceDoesNotExistException();
        }
      }
    }

    return em.merge(analysis);
  }

}
