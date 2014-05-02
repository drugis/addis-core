package org.drugis.addis.analyses;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by connor on 3/11/14.
 */
public class AnalysisTypeTest {

  @Test
  public void testGetByLabel() throws Exception {
    assertEquals(AnalysisType.SINGLE_STUDY_BENEFIT_RISK, AnalysisType.getByLabel("Single-study Benefit-Risk"));
    assertEquals(AnalysisType.NETWORK_META_ANALYSIS, AnalysisType.getByLabel("Network meta-analysis"));
  }

  @Test(expected = Exception.class)
  public void testGetByNonsenseLabelFails() throws Exception {
    AnalysisType.getByLabel("something serious but wrong");
  }

  @Test
  public void testGetLabelEqualsToString() {
    assertEquals(AnalysisType.SINGLE_STUDY_BENEFIT_RISK.getLabel(), AnalysisType.SINGLE_STUDY_BENEFIT_RISK.toString());
    assertEquals(AnalysisType.NETWORK_META_ANALYSIS.getLabel(), AnalysisType.NETWORK_META_ANALYSIS.toString());
  }
}
