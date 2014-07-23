package org.drugis.addis.trialverse.repository.impl;


import org.drugis.addis.trialverse.model.*;
import org.drugis.addis.trialverse.repository.TrialverseRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.*;

/**
 * Created by connor on 2/26/14.
 */
@Repository
public class TrialverseRepositoryImpl implements TrialverseRepository {

  @PersistenceContext(unitName = "trialverse")
  @Qualifier("emTrialverse")
  EntityManager em;

  @Override
  public List<Arm> getArmsByDrugIds(String studyId, Collection<String> drugIds) {
    if (drugIds.isEmpty()) {
      return Collections.emptyList();
    }
    Query query = em.createNativeQuery("SELECT" +
            " a.id, a.name, t.drug" +
            " FROM" +
            "  arms a," +
            "  designs d," +
            "  treatments t," +
            "  measurement_moments mm" +
            " WHERE" +
            "  a.id = d.arm" +
            " AND" +
            "  d.activity = t.activity" +
            " AND" +
            "  mm.is_primary = true" +
            " AND" +
            "  mm.epoch = d.epoch" +
            " AND" +
            " t.drug IN :drugIds", Arm.class);
    query.setParameter("drugIds", drugIds);
    return query.getResultList();
  }

  @Override
  public List<TrialDataArm> getArmsForStudies(Long namespaceId, List<Long> studyIds, List<Variable> variables) {
    List<Long> variableIds = new ArrayList<>(variables.size());
    for (Variable variable : variables) {
      variableIds.add(variable.getId());
    }

    Query query = em.createNativeQuery(
            "select " +
                    " a.id," +
                    " a.name," +
                    " a.study," +
                    " t.drug as drug" +
                    " FROM arms a RIGHT OUTER JOIN designs d ON (a.id = d.arm)," +
                    " namespace_studies nss," +
                    " measurement_moments mm," +
                    " measurements m," +
                    " activities ac," +
                    " treatments t" +
                    " WHERE a.name != ''" +
                    " and d.arm = a.id " +
                    " and d.epoch = mm.epoch" +
                    " and a.id = m.arm" +
                    " and mm.is_primary = true" +
                    " and ac.id = d.activity" +
                    " and t.activity = ac.id" +
                    " and nss.namespace = :namespaceId" +
                    " and a.study IN :studyIds" +
                    " and m.variable IN :variableIds" +
                    " and a.id NOT IN (select distinct" +
                    "  arm.id" +
                    "  from " +
                    "  arms arm, designs d, activities act, treatments t" +
                    "   where " +
                    "     d.arm = arm.id " +
                    "    and act.id = d.activity" +
                    "    and t.activity = act.id" +
                    "    group by arm.id" +
                    "    having count(arm.id) > 1)", TrialDataArm.class
    );

    query.setParameter("namespaceId", namespaceId);
    query.setParameter("studyIds", studyIds);
    query.setParameter("variableIds", variableIds);
    List<TrialDataArm> arms = query.getResultList();
    return new ArrayList<>(new HashSet<>(arms));
  }

  @Override
  public List<Variable> getVariablesByOutcomeIds(Set<String> outcomeIds) {
    if (outcomeIds.isEmpty()) {
      return Collections.emptyList();
    }
    TypedQuery<Variable> query = em.createQuery("FROM Variable v WHERE v.id IN :outcomeIds", Variable.class);
    query.setParameter("outcomeIds", outcomeIds);
    return query.getResultList();
  }

  @Override
  public List<Measurement> getOrderedMeasurements(List<String> outcomeUds, List<String> armIds) {
    if (outcomeUds.isEmpty() || armIds.isEmpty()) {
      return Collections.emptyList();
    }

    Query query = em.createNativeQuery("SELECT" +
            "  m.*" +
            " FROM" +
            "  measurements m," +
            "  measurement_moments mm," +
            "  arms a" +
            " WHERE" +
            "  a.id = m.arm" +
            " AND" +
            "  mm.id = m.measurement_moment" +
            " AND" +
            "  mm.is_primary = TRUE" +
            " AND" +
            "  m.variable IN :outcomeIds" +
            " AND" +
            "  m.arm IN :armIds" +
            " ORDER BY m.variable, m.arm, m.attribute", Measurement.class);
    query.setParameter("outcomeIds", outcomeUds);
    query.setParameter("armIds", armIds);
    return query.getResultList();
  }

  @Override
  public List<Measurement> getStudyMeasurementsForOutcomes(Collection<Long> studyIds, Collection<Long> outcomeIds, Collection<Long> armIds) {
    if (studyIds.isEmpty() || outcomeIds.isEmpty() || armIds.isEmpty()) {
      return Collections.emptyList();
    }

    Query query = em.createNativeQuery("SELECT" +
            "  m.*" +
            " FROM" +
            "  measurements m," +
            "  measurement_moments mm," +
            "  arms a" +
            " WHERE" +
            "  a.id = m.arm" +
            " AND" +
            "  mm.id = m.measurement_moment" +
            " AND" +
            "  mm.is_primary = TRUE" +
            " AND" +
            "  m.variable IN :outcomeIds" +
            " AND" +
            "  m.arm IN :armIds" +
            " AND" +
            " m.study in :studyIds" +
            " ORDER BY m.variable, m.arm, m.attribute", Measurement.class);
    query.setParameter("studyIds", studyIds);
    query.setParameter("outcomeIds", outcomeIds);
    query.setParameter("armIds", armIds);
    return query.getResultList();
  }

  @Override
  public List<Study> getStudiesByIds(String namespaceUid, List<String> studyUids) {
    if (studyUids.isEmpty()) {
      return Collections.EMPTY_LIST;
    }
    Query query = em.createNativeQuery("select" +
                    " s.id, s.name, s.title " +
                    " FROM" +
                    " studies s," +
                    " namespaces ns," +
                    " namespace_studies nss" +
                    " WHERE nss.namespace = ns.id " +
                    " AND nss.study = s.id" +
                    " AND nss.namespace = :namespaceId" +
                    " AND s.id IN :studyIds",
            Study.class
    );
    query.setParameter("namespaceId", namespaceUid);
    query.setParameter("studyIds", studyUids);
    return query.getResultList();
  }

  @Override
  public List<Study> queryStudies(String namespaceUid) {
    Query query = em.createNativeQuery("select" +
                    " s.id, s.name, s.title " +
                    " FROM" +
                    " studies s," +
                    " namespaces ns," +
                    " namespace_studies nss" +
                    " WHERE nss.namespace = ns.id " +
                    " AND nss.study = s.id" +
                    " AND nss.namespace = :namespaceId",
            Study.class
    );
    query.setParameter("namespaceId", namespaceUid);
    return query.getResultList();
  }

}
