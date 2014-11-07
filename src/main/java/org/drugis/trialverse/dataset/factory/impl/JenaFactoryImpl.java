package org.drugis.trialverse.dataset.factory.impl;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.drugis.trialverse.dataset.factory.JenaFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by daan on 6-11-14.
 */
@Service
public class JenaFactoryImpl implements JenaFactory {

  private final static String TRIPLESTORE_URI = System.getenv("TRIPLESTORE_BASE_URI") + "/current/data";

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
  public Model createModel() {
    return ModelFactory.createDefaultModel();
  }

}

