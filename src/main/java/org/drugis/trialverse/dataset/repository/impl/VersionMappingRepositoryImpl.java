package org.drugis.trialverse.dataset.repository.impl;

import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.security.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by connor on 10-3-15.
 */
@Repository
public class VersionMappingRepositoryImpl implements VersionMappingRepository{

    private final static Logger logger = LoggerFactory.getLogger(DatasetWriteRepositoryImpl.class);

    private RowMapper<VersionMapping> rowMapper = new RowMapper<VersionMapping>() {
        public VersionMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new VersionMapping(rs.getInt("id"), rs.getString("datasetUuid"), rs.getString("ownerUuid"), rs.getString("versionKey"));
        }
    };

    @Inject
    @Qualifier("jtTrialverse")
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional()
    public void createMapping(VersionMapping versionMapping) {
        try{
            jdbcTemplate.update("insert into VersionMapping (datasetUuid, ownerUuid, versionKey) values (?, ?, ?)",
                    versionMapping.getDatasetUuid(), versionMapping.getOwnerUuid(), versionMapping.getVersionKey());
        } catch (DuplicateKeyException e) {
            logger.error("duplicate mapping key");
            throw new RuntimeException("duplicate mapping key");
        }
    }

    @Override
    public VersionMapping findMappingByUsername(String username) {
        return null;
    }

    @Override
    public VersionMapping findMappingByVersionKey(String key) {
        return null;
    }
}
