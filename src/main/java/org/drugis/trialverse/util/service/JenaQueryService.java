package org.drugis.trialverse.util.service;

import org.apache.jena.query.QueryExecution;

/**
 * Created by daan on 2-9-15.
 */
public interface JenaQueryService {
  QueryExecution query(String s, String query);
}
