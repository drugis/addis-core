package org.drugis.addis.patavitask.repository;

import com.fasterxml.jackson.databind.JsonNode;
import org.drugis.addis.models.Model;
import org.drugis.addis.patavitask.PataviTask;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;

import java.io.IOException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by connor on 26-6-14.
 */

public interface PataviTaskRepository {

  URI createPataviTask(NetworkMetaAnalysisProblem problem, Model model) throws IOException, SQLException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException;

  List<PataviTask> findByIds(List<URI> taskUris) throws SQLException;

  JsonNode getResult(URI taskId) throws IOException, UnexpectedNumberOfResultsException;

  Map<Integer, JsonNode> getResults(List<URI> taskUris) throws SQLException, IOException;

  void delete(URI taskUrl);
}
