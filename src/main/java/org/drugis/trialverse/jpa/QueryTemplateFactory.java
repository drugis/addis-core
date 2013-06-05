package org.drugis.trialverse.jpa;

import org.drugis.trialverse.jpa.CachedQueryTemplateFactory.QueryTemplate;
import org.springframework.stereotype.Service;

@Service
public interface QueryTemplateFactory {

	public QueryTemplate buildQueryTemplate(String templateFile);

}