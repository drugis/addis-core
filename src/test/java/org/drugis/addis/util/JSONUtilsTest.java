package org.drugis.addis.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by daan on 28-3-14.
 */
public class JSONUtilsTest {
  @Test
  public void testCreateKey() throws Exception {
    String withParentheses = "SBP mean trough (clinic, sitting)";
    String expectedWithParentheses = "sbp-mean-trough-clinic-sitting";
    String withDosage = "Azilsartan 20.0 mg/day";
    String expectedWithDosage="azilsartan-200-mgday";

    assertEquals(expectedWithParentheses, JSONUtils.createKey(withParentheses));
    assertEquals(expectedWithDosage, JSONUtils.createKey(withDosage));
  }
}
