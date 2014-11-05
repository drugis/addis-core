package org.drugis.trialverse.dataset.repository.impl;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.drugis.trialverse.dataset.repository.DatasetRepository;
import org.drugis.trialverse.security.Account;

import java.util.UUID;

/**
 * Created by connor on 04/11/14.
 */
public class DatasetRepositoryImpl implements DatasetRepository {

  public final static String DATASET = "http://trials.drugis.org/datasets/";
  public final String DC_CREATOR = "http://purl.org/dc/elements/1.1/creator";

  @Override
  public String createDataset(String title, String description, Account owner) {

    String uuid = UUID.randomUUID().toString();
    String datasetIdentifier = DATASET + uuid;

    DatasetGraph datasetGraph = DatasetGraphFactory.createMem();
    Graph graph = GraphFactory.createGraphMem();

    Node datasetURI = NodeFactory.createURI(datasetIdentifier);
    Node creatorRel = NodeFactory.createURI(DC_CREATOR);
    Node creatorName = NodeFactory.createLiteral(owner.getUsername());

    graph.add(new Triple(datasetURI, creatorRel, creatorName));

    datasetGraph.addGraph(datasetURI, graph);

    RDFDataMgr.createDatasetWriter(RDFLanguages.TRIG).write(System.out, datasetGraph, null, null, null);

    return datasetIdentifier;
  }




}
