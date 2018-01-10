package org.drugis.addis.interventions.model;

import org.junit.Test;

import java.net.URI;

public class LowerDoseBoundTest {
  @Test
  public void createLowerDoseBoundTest() throws InvalidConstraintException {
    new LowerDoseBound(LowerBoundType.AT_LEAST, 5., "unitName","P1W", URI.create("someURI"), 0.001);
  }
  @Test(expected = InvalidConstraintException.class)
  public void createInvalidTypeLowerDoseBoundTest() throws InvalidConstraintException {
    new LowerDoseBound(null, 5., "unitName","P1W", URI.create("someURI"), 0.001);
  }
  @Test(expected = InvalidConstraintException.class)
  public void createInvalidValueLowerDoseBoundTest() throws InvalidConstraintException {
    new LowerDoseBound(LowerBoundType.AT_LEAST, null, "unitName","P1W", URI.create("someURI"), 0.001);
  }
  @Test(expected = InvalidConstraintException.class)
  public void createInvalidUnitNameLowerDoseBoundTest() throws InvalidConstraintException {
    new LowerDoseBound(LowerBoundType.AT_LEAST, 5., null,"P1W", URI.create("someURI"), 0.001);
  }
  @Test(expected = InvalidConstraintException.class)
  public void createInvalidPeriodLowerDoseBoundTest() throws InvalidConstraintException {
    new LowerDoseBound(LowerBoundType.AT_LEAST, 5., "unitName",null, URI.create("someURI"), 0.001);
  }


}