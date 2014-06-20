package org.drugis.addis.analyses.repository.impl;

import org.drugis.addis.analyses.AnalysisCommand;
import org.drugis.addis.analyses.ArmExclusion;
import org.drugis.addis.analyses.InterventionExclusion;
import org.drugis.addis.analyses.NetworkMetaAnalysis;
import org.drugis.addis.analyses.repository.NetworkMetaAnalysisRepository;
import org.drugis.addis.exception.MethodNotAllowedException;
import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.models.repositories.ModelRepository;
import org.drugis.addis.security.Account;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by connor on 6-5-14.
 */
@Repository
public class NetworkMetaAnalysisRepositoryImpl implements NetworkMetaAnalysisRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  EntityManager em;

  @Inject
  AnalysisRepositoryUtils analysisRepositoryUtils;

  @Inject
  ModelRepository modelRepository;

  @Override
  public NetworkMetaAnalysis create(Account user, AnalysisCommand analysisCommand) throws MethodNotAllowedException, ResourceDoesNotExistException {
    NetworkMetaAnalysis networkMetaAnalysis = new NetworkMetaAnalysis(analysisCommand.getProjectId(), analysisCommand.getName());
    analysisRepositoryUtils.checkProjectExistsAndModifiable(user, analysisCommand.getProjectId(), em);
    em.persist(networkMetaAnalysis);
    return networkMetaAnalysis;
  }

  @Override
  public Collection<NetworkMetaAnalysis> query(Integer projectId) {
    TypedQuery<NetworkMetaAnalysis> query = em.createQuery("FROM NetworkMetaAnalysis a WHERE a.projectId = :projectId", NetworkMetaAnalysis.class);
    query.setParameter("projectId", projectId);
    return query.getResultList();
  }

  @Override
  public NetworkMetaAnalysis update(Account user, NetworkMetaAnalysis analysis) throws ResourceDoesNotExistException, MethodNotAllowedException {
    Integer analysisProjectId = analysis.getProjectId();
    analysisRepositoryUtils.checkProjectExistsAndModifiable(user, analysisProjectId, em);

    if (modelRepository.findByAnalysis(analysis) != null) {
      // can not update locked exception
      throw new MethodNotAllowedException();
    }

    // do not allow changing of project ID
    NetworkMetaAnalysis oldAnalysis = em.find(NetworkMetaAnalysis.class, analysis.getId());
    if (!oldAnalysis.getProjectId().equals(analysisProjectId)) {
      throw new ResourceDoesNotExistException();
    }

    // do not allow selection of outcome that is not in the project
    if (analysis.getOutcome() != null && !analysis.getOutcome().getProject().equals(analysisProjectId)) {
      throw new ResourceDoesNotExistException();
    }

    // remove old
    Query deleteArmExclusionsQuery = em.createQuery("delete from ArmExclusion ae where ae.analysisId = :analysisId");
    deleteArmExclusionsQuery.setParameter("analysisId", analysis.getId());
    deleteArmExclusionsQuery.executeUpdate();

    Query deleteInterventionExclusionsQuery = em.createQuery("delete from InterventionExclusion ie where ie.analysisId = :analysisId");
    deleteInterventionExclusionsQuery.setParameter("analysisId", analysis.getId());
    deleteInterventionExclusionsQuery.executeUpdate();

    // add new
    List<ArmExclusion> newArmExclusionList = new ArrayList<>();
    for (ArmExclusion armExclusion : analysis.getExcludedArms()) {
      ArmExclusion newArmExclusion = new ArmExclusion(armExclusion.getAnalysisId(), armExclusion.getTrialverseId());
      em.persist(newArmExclusion);
      newArmExclusionList.add(newArmExclusion);
    }

    List<InterventionExclusion> newInterventionExclusionList = new ArrayList<>();
    for (InterventionExclusion interventionExclusion : analysis.getExcludedInterventions()) {
      InterventionExclusion newInterventionExclusion = new InterventionExclusion(interventionExclusion.getAnalysisId(), interventionExclusion.getInterventionId());
      em.persist(newInterventionExclusion);
      newInterventionExclusionList.add(newInterventionExclusion);
    }

    analysis.setExcludedArms(newArmExclusionList);
    analysis.setExcludedInterventions(newInterventionExclusionList);

    return em.merge(analysis);
  }
}
