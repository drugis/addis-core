package org.drugis.trialverse.dataset.repository.impl;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import org.drugis.trialverse.dataset.repository.DatasetRepository;
import org.drugis.trialverse.security.Account;

/**
 * Created by connor on 04/11/14.
 */
public class DatasetRepositoryImpl implements DatasetRepository {

  private final static String BASE_UIR = "http://trialverse/dataset";

  @Override
  public String createDataset(String title, String description, Account owner) {

    //create an empty Model
    Model model = ModelFactory.createDefaultModel();

    // create the resource
    Resource dataset = model.createResource(BASE_UIR + "/" + title);

    // add the property
    dataset.addProperty(  )

    model.write(System.out, "TURTLE");

    return "jep jep";
  }




}
