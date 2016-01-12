package org.drugis.addis.trialverse.model.emun;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by connor on 10/29/15.
 */
public class StudyStatusEnumTest {

    @Test
    public void testFromString() {
      assertEquals(StudyStatusEnum.ACTIVE, StudyStatusEnum.fromString("StatusActive"));
      assertEquals(StudyStatusEnum.COMPLETED, StudyStatusEnum.fromString("StatusCompleted"));
      assertEquals(StudyStatusEnum.ENROLLING, StudyStatusEnum.fromString("StatusEnrolling"));
      assertEquals(StudyStatusEnum.SUSPENDED, StudyStatusEnum.fromString("StatusSuspended"));
      assertEquals(StudyStatusEnum.TERMINATED, StudyStatusEnum.fromString("StatusTerminated"));
      assertEquals(StudyStatusEnum.UNKNOWN, StudyStatusEnum.fromString("StatusUnknown"));
      assertEquals(StudyStatusEnum.WITHDRAWN, StudyStatusEnum.fromString("StatusWithdrawn"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownStringThrowsException() {
      StudyStatusEnum.fromString("nonsense");
    }

  }
