package org.drugis.trialverse.jpa;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NoArgsConstructor;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

@NoArgsConstructor
public class CachedQueryTemplateFactory implements QueryTemplateFactory {
	
	private Map<String, QueryTemplate> d_templateCache = new ConcurrentHashMap<String, QueryTemplate>();

	@Override
	public QueryTemplate buildQueryTemplate(String templateFile) { 
		QueryTemplate template;
		if(d_templateCache.containsKey(templateFile)) { 
			template = d_templateCache.get(templateFile);
		} else {
			template = new QueryTemplate(templateFile);
			d_templateCache.put(templateFile, template);
		}
		return template;
	}
	
	public static class QueryTemplate { 
		private String d_template;
	
		public QueryTemplate(String resourceName) {
			try {
				d_template = Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8);
			} catch(IOException e) { 
				throw new RuntimeException("Could not create query template for " + resourceName, e);
			}
		}
	
		public String getTemplate() {
			return d_template;
		}
	}
}
