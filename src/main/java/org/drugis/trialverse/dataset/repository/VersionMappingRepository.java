package org.drugis.trialverse.dataset.repository;

import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.security.UsernameAlreadyInUseException;

import java.util.List;

/**
 * Created by connor on 10-3-15.
 */
public interface VersionMappingRepository {

    void save(VersionMapping versionMapping);

    List<VersionMapping> findMappingsByUsername(String username);

    VersionMapping findMappingByVersionKey(String key);
}
