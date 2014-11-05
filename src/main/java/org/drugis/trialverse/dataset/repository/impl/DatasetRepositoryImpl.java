package org.drugis.trialverse.dataset.repository.impl;

import com.hp.hpl.jena.query.DatasetAccessor;
import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.DC;

import org.drugis.trialverse.dataset.repository.DatasetRepository;
import org.drugis.trialverse.security.Account;

import java.util.UUID;

/**
 * Created by connor on 04/11/14.
 */


public class DatasetRepositoryImpl implements DatasetRepository {
  public static String eTag = null;


  @Override
  public String createDataset(String title, String description, Account owner) {

    DatasetAccessor dataAccessor = DatasetAccessorFactory.createHTTP("http://localhost:3030/current/data");

    String uuid = UUID.randomUUID().toString();
    String datasetIdentifier = DATASET + uuid;

    Model model = ModelFactory.createDefaultModel();

    Resource datasetURI = model.createResource(datasetIdentifier);
    Property creatorRel = DC.creator;
    Literal creatorName = model.createLiteral(owner.getUsername());

    model.add(datasetURI, creatorRel, creatorName);

    dataAccessor.putModel(datasetIdentifier, model);

//    doPUT(new HttpClient(), "http://localhost:3030/current/data/datasets/" + uuid, out.toString());

    return datasetIdentifier;
  }




}
