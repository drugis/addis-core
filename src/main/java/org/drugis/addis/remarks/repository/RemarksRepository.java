package org.drugis.addis.remarks.repository;

import org.drugis.addis.remarks.Remarks;

/**
 * Created by daan on 16-9-14.
 */
public interface RemarksRepository {
  public Remarks find(Integer analysisId);

  public Remarks update(Remarks remarks);

  public Remarks create(Integer analysisId, String remarks);
}
