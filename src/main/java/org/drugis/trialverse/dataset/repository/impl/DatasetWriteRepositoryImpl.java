package org.drugis.trialverse.dataset.repository.impl;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
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

  private Model createDatasetModel(String datasetIdentifier, Account owner, String title, String description) {
    Model model = jenaFactory.createModel();
    model.setNsPrefix("ontology", "http://trials.drugis.org/ontology#");
    Resource datasetURI = model.createResource(datasetIdentifier);
    Resource datasetOntologyURI = model.createResource("http://trials.drugis.org/ontology#Dataset");
    if (description != null) {
      model.add(datasetURI, RDFS.comment, description);
    }

    return model
            .add(datasetURI, DC.creator, owner.getUsername())
            .add(datasetURI, RDFS.label, title)
            .add(datasetURI, RDF.type, datasetOntologyURI);
  }

  @Override
  public String createDataset(String title, String description, Account owner) {
    DatasetAccessor dataSetAccessor = jenaFactory.getDatasetAccessor();
    String datasetIdentifier = jenaFactory.createDatasetURI();
    Model model = createDatasetModel(datasetIdentifier, owner, title, description);
    dataSetAccessor.putModel(datasetIdentifier, model);

    return datasetIdentifier;
  }

  @Override
  public void updateDataset()
}
