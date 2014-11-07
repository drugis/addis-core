package org.drugis.trialverse.dataset.repository.impl;

import com.hp.hpl.jena.vocabulary.RDF;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.security.Account;

import javax.inject.Inject;

/**
 * Created by daan on 7-11-14.
 */
public class DatasetReadRepositoryImpl implements DatasetReadRepository {

  @Inject


  @Override
  public RDF query(Account currentUserAccount) {
    return null;
  }
}
