package org.drugis.addis.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Created by connor on 2/27/14.
 */
@Configuration
@ComponentScan(basePackages = {"org.drugis.addis.trialverse", "org.drugis.trialverse"}, excludeFilters = {@ComponentScan.Filter(Configuration.class)})
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"org.drugis.addis.trialverse", "org.drugis.trialverse"})
public class TrialverseConfig {


}
