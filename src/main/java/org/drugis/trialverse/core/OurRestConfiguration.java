package org.drugis.trialverse.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.rest.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

@Configuration
@Import(RepositoryRestMvcConfiguration.class)
public class OurRestConfiguration {
	/**
	 * Main configuration for the REST exporter.
	 */
	@Bean public RepositoryRestConfiguration config() {
		final RepositoryRestConfiguration config = new RepositoryRestConfiguration();
		configureRepositoryRestConfiguration(config);
		return config;
	}

	/**
	 * Override this method to add additional configuration.
	 *
	 * @param config
	 * 		Main configuration bean.
	 */
	protected void configureRepositoryRestConfiguration(final RepositoryRestConfiguration config) {
	}
}
