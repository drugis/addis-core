package org.drugis.trialverse.queries;

import org.drugis.trialverse.queries.CachedQueryTemplateFactory.QueryTemplate;

public interface QueryTemplateFactory {

	public QueryTemplate buildQueryTemplate(String templateFile);

}