package org.drugis.addis.remarks.repository;

import org.drugis.addis.remarks.Remarks;

/**
 * Created by daan on 16-9-14.
 */
public interface RemarksRepository {
    public Remarks get(Integer scenarioId);
}
