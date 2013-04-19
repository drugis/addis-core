package org.drugis.trialverse;

import org.drugis.trialverse.CachedQueryTemplateFactory.QueryTemplate;

public interface QueryTemplateFactory {

	public abstract QueryTemplate buildQueryTemplate(String templateFile);

}