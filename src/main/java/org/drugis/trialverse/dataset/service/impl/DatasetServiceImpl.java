package org.drugis.trialverse.dataset.service.impl;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.DC;
import org.drugis.trialverse.dataset.service.DatasetService;
import org.drugis.trialverse.security.Account;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by daan on 6-11-14.
 */
@Service
public class DatasetServiceImpl implements DatasetService {

  private final static String TRIPLESTORE_URI = System.getenv("TRIPLESTORE_BASE_URI") + "current/data";

  @Override
  public DatasetAccessor getDatasetAccessor() {
    return DatasetAccessorFactory.createHTTP(TRIPLESTORE_URI);
  }

  @Override
  public String createDatasetURI() {
    String uuid = UUID.randomUUID().toString();
    return DATASET + uuid;
  }

  @Override
  public Model createDatasetModel(Account owner, String datasetIdentifier) {
    Model model = ModelFactory.createDefaultModel();

    Resource datasetURI = model.createResource(datasetIdentifier);
    Property creatorRel = DC.creator;
    Literal creatorName = model.createLiteral(owner.getUsername());

    return model.add(datasetURI, creatorRel, creatorName);
  }

}
