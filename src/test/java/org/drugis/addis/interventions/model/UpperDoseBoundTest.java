package org.drugis.addis.interventions.model;

import org.junit.Test;

import java.net.URI;

public class UpperDoseBoundTest {
@Test
    public void createUpperDoseBoundTest() throws InvalidConstraintException {
      new UpperDoseBound(UpperBoundType.AT_MOST, 5., "unitName","P1W", URI.create("someURI"), 0.001);
    }
    @Test(expected = InvalidConstraintException.class)
    public void createInvalidTypeUpperDoseBoundTest() throws InvalidConstraintException {
      new UpperDoseBound(null, 5., "unitName","P1W", URI.create("someURI"), 0.001);
    }
    @Test(expected = InvalidConstraintException.class)
    public void createInvalidValueUpperDoseBoundTest() throws InvalidConstraintException {
      new UpperDoseBound(UpperBoundType.AT_MOST, null, "unitName","P1W", URI.create("someURI"), 0.001);
    }
    @Test(expected = InvalidConstraintException.class)
    public void createInvalidUnitNameUpperDoseBoundTest() throws InvalidConstraintException {
      new UpperDoseBound(UpperBoundType.AT_MOST, 5., null,"P1W", URI.create("someURI"), 0.001);
    }
    @Test(expected = InvalidConstraintException.class)
    public void createInvalidPeriodUpperDoseBoundTest() throws InvalidConstraintException {
      new UpperDoseBound(UpperBoundType.AT_MOST, 5., "unitName",null, URI.create("someURI"), 0.001);
    }
}
