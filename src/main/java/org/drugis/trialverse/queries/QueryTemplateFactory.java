package org.drugis.trialverse.queries;

import org.drugis.trialverse.queries.CachedQueryTemplateFactory.QueryTemplate;
import org.springframework.stereotype.Service;

@Service
public interface QueryTemplateFactory {

	public QueryTemplate buildQueryTemplate(String templateFile);

}