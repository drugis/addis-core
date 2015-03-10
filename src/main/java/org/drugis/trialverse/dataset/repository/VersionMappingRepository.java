package org.drugis.trialverse.dataset.repository;

import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.security.Account;
import org.drugis.trialverse.security.UsernameAlreadyInUseException;

/**
 * Created by connor on 10-3-15.
 */
public interface VersionMappingRepository {

    void createMapping(VersionMapping versionMapping);

    VersionMapping findMappingByUsername(String username);

    VersionMapping findMappingByVersionKey(String key);
}
