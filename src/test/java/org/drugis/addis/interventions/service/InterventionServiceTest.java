package org.drugis.addis.interventions.service;

import org.drugis.addis.interventions.model.AbstractIntervention;
import org.drugis.addis.interventions.model.SimpleIntervention;
import org.drugis.addis.trialverse.model.AbstractSemanticIntervention;
import org.drugis.addis.trialverse.model.Measurement;
import org.drugis.addis.trialverse.model.SimpleSemanticIntervention;
import org.drugis.addis.trialverse.model.TrialDataArm;
import org.junit.Test;
import org.mockito.InjectMocks;

import java.net.URI;

import static org.junit.Assert.assertTrue;

/**
 * Created by connor on 12-4-16.
 */
public class InterventionServiceTest {

  @InjectMocks
  private InterventionService interventionService;

  @Test
  public void isMatched() throws Exception {

    Integer interventionId = 2;
    Integer projectId = 1;

    Integer sampleSize = 200;
    Long rate = 30l;
    Double std = 0.5;
    Double mean = 30.2;

    URI drugConceptUri = URI.create("drugConceptUri");
    AbstractIntervention intervention = new SimpleIntervention(interventionId, projectId, "intervention name", "moti", drugConceptUri, "sem label");


    AbstractSemanticIntervention semanticIntervention = new SimpleSemanticIntervention(URI.create("drugInstanceUri"), drugConceptUri);
    Measurement measurement = new Measurement(URI.create("studyUri"), URI.create("variableUri"), URI.create("armUri"), sampleSize, rate, std, mean);
    TrialDataArm trialdataArm = new TrialDataArm(URI.create("uri"), "arm name", URI.create("instance"), measurement, semanticIntervention);

    boolean result = interventionService.isMatched(intervention, trialdataArm);
    assertTrue(result);
  }

}