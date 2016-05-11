package org.drugis.addis.patavitask.service;

import org.drugis.addis.exception.ResourceDoesNotExistException;
import org.drugis.addis.interventions.service.impl.InvalidTypeForDoseCheckException;
import org.drugis.addis.models.exceptions.InvalidModelException;
import org.drugis.addis.patavitask.PataviTaskUriHolder;
import org.drugis.addis.trialverse.service.impl.ReadValueException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.SQLException;

/**
 * Created by connor on 26-6-14.
 */
public interface PataviTaskService {
  PataviTaskUriHolder getPataviTaskUriHolder(Integer projectId, Integer analysisId, Integer modelId) throws ResourceDoesNotExistException, IOException, SQLException, InvalidModelException, URISyntaxException, ReadValueException, InvalidTypeForDoseCheckException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException;
}
