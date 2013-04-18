package org.drugis.trialverse;

import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

public class QueryTemplate {
	private String d_template;

	public QueryTemplate(String resourceName) throws IOException {
		d_template = Resources.toString(Resources.getResource(resourceName), Charsets.UTF_8);
	}

	public String getTemplate() {
		return d_template;
	}
}
