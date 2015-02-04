package org.drugis.trialverse.study.repository;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * Created by connor on 28-11-14.
 */
public interface StudyReadRepository {

  public Model getStudy(String studyUUID);
}
