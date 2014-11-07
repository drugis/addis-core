package org.drugis.trialverse.dataset.repository.impl;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.drugis.trialverse.dataset.repository.DatasetWriteRepository;
import org.drugis.trialverse.security.Account;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

/**
 * Created by connor on 04/11/14.
 */

@Repository
public class DatasetWriteRepositoryImpl implements DatasetWriteRepository {

  @Inject
  private JenaFactory jenaFactory;

  private Model createDatasetModel(Account owner, String datasetIdentifier) {
    Model model = jenaFactory.createModel();

    Resource datasetURI = model.createResource(datasetIdentifier);
    Property creatorRel = DC.creator;
    Literal creatorName = model.createLiteral(owner.getUsername());

    return model.add(datasetURI, creatorRel, creatorName);
  }


  @Override
  public String createDataset(String title, String description, Account owner) {
    DatasetAccessor dataSetAccessor = jenaFactory.getDatasetAccessor();
    String datasetIdentifier = jenaFactory.createDatasetURI();
    Model model = createDatasetModel(owner, datasetIdentifier);
    dataSetAccessor.putModel(datasetIdentifier, model);

    return datasetIdentifier;
  }


}
