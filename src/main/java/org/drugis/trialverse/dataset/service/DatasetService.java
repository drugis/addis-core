package org.drugis.trialverse.dataset.service;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.rdf.model.Model;
import org.drugis.trialverse.security.Account;

/**
 * Created by daan on 6-11-14.
 */
public interface DatasetService {
  public final static String DATASET = "http://trials.drugis.org/datasets/";

  public DatasetAccessor getDatasetAccessor();

  public String createDatasetURI();

  public Model createDatasetModel(Account owner, String datasetIdentifier);
}
