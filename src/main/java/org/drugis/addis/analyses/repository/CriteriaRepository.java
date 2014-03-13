package org.drugis.addis.analyses.repository;

import org.drugis.addis.analyses.Criterion;

import java.util.List;

/**
 * Created by connor on 3/13/14.
 */
public interface CriteriaRepository {
  List<Criterion> query(Integer projectId, Integer analysisId);

  Criterion create(Criterion criterion);
}
