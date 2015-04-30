package org.drugis.addis.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan(excludeFilters = {@ComponentScan.Filter(Configuration.class)}, basePackages = {
        "org.drugis.addis.trialverse.service"
})
public class TrialverseServiceTestConfig {

}

