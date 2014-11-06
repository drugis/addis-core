package org.drugis.trialverse.dataset.repository.impl;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.rdf.model.Model;
import org.drugis.trialverse.dataset.repository.DatasetRepository;
import org.drugis.trialverse.dataset.service.DatasetService;
import org.drugis.trialverse.security.Account;

import javax.inject.Inject;

/**
 * Created by connor on 04/11/14.
 */

public class DatasetRepositoryImpl implements DatasetRepository {

  @Inject
  private DatasetService datasetService;

  @Override
  public String createDataset(String title, String description, Account owner) {

    DatasetAccessor dataSetAccessor = datasetService.getDatasetAccessor();

    String datasetIdentifier = datasetService.createDatasetURI();

    Model model = datasetService.createDatasetModel(owner, datasetIdentifier);

    dataSetAccessor.putModel(datasetIdentifier, model);

    return datasetIdentifier;
  }



}
