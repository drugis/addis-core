package org.drugis.trialverse.study.repository.impl;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.rdf.model.Model;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.study.repository.StudyReadRepository;
import org.drugis.trialverse.util.Namespaces;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

/**
 * Created by connor on 28-11-14.
 */
@Repository
public class StudyReadRepositoryImpl implements StudyReadRepository {

  @Inject
  private JenaFactory jenaFactory;

  @Override
  public Model getStudy(String studyUUID) {
    DatasetAccessor datasetAccessor = jenaFactory.getDatasetAccessor();
    return datasetAccessor.getModel(Namespaces.STUDY_NAMESPACE + studyUUID);
  }
}
