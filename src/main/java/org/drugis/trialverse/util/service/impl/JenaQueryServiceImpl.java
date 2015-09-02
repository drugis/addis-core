package org.drugis.trialverse.util.service.impl;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.drugis.trialverse.util.service.JenaQueryService;
import org.springframework.stereotype.Service;

/**
 * Created by daan on 2-9-15.
 */
@Service
public class JenaQueryServiceImpl implements JenaQueryService {
  @Override
  public QueryExecution query(String serviceUri, String query) {
    return QueryExecutionFactory.sparqlService(serviceUri, query);
  }
}
