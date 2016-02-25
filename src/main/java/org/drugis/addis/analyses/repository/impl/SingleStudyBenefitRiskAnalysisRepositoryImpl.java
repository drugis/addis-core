package org.drugis.addis.analyses.repository.impl;

import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.SingleStudyBenefitRiskAnalysis;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.Intervention;
import org.drugis.addis.outcomes.Outcome;
import org.drugis.addis.projects.Project;
import org.drugis.addis.security.Account;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.Collection;
import java.util.Collections;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * Created by connor on 3/11/14.
 */
@Repository
public class SingleStudyBenefitRiskAnalysisRepositoryImpl implements org.drugis.addis.analyses.repository.SingleStudyBenefitRiskAnalysisRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

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
    Project project = em.find(Project.class, analysis.getProjectId());
    if (project == null) {
      throw new ResourceDoesNotExistException();
    }
    if (!project.getOwner().getId().equals(user.getId())) {
      throw new MethodNotAllowedException();
    }

    Integer analysisProjectId = analysis.getProjectId();

    // do not allow changing of project ID
    SingleStudyBenefitRiskAnalysis oldAnalysis = em.find(SingleStudyBenefitRiskAnalysis.class, analysis.getId());
    if (!oldAnalysis.getProjectId().equals(analysisProjectId)) {
      throw new ResourceDoesNotExistException();
    }

    if (isNotEmpty(analysis.getSelectedOutcomes())) {
      // do not allow selection of outcomes that are not in the project
      for (Outcome outcome : analysis.getSelectedOutcomes()) {
        if (!outcome.getProject().equals(analysisProjectId)) {
          throw new ResourceDoesNotExistException();
        }
      }
    }

    if (isNotEmpty(analysis.getSelectedInterventions())) {
      // do not allow selection of interventions that are not in the project
      for (Intervention intervention : analysis.getSelectedInterventions()) {
        if (!intervention.getProject().equals(analysisProjectId)) {
          throw new ResourceDoesNotExistException();
        }
      }
    }

    return em.merge(analysis);
  }

}
