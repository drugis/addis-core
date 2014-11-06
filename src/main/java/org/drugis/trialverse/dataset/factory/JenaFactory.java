package org.drugis.trialverse.dataset.factory;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.rdf.model.Model;

/**
 * Created by daan on 6-11-14.
 */
public interface JenaFactory {
  public final static String DATASET = "http://trials.drugis.org/datasets/";

  public DatasetAccessor getDatasetAccessor();

  public String createDatasetURI();

  public Model createModel();
}
