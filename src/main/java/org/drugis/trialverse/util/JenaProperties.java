package org.drugis.trialverse.util;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.RDFSyntax;
import org.springframework.stereotype.Component;

/**
 * Created by connor on 9/8/15.
 */
@Component
public class JenaProperties {
  private static final String GRAPH_REVISION = "graph_revision";
  private static final String MERGED_REVISION = "merged_revision";

  private static final String DATASET = "dataset";
  private static final String DATASET_VERSION = "DatasetVersion";
  private static final String HEAD = "head";
  private static final String PREVIOUS = "previous";
  private static final String RDF_TYPE_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
  private static final String DC_TITLE = "http://purl.org/dc/terms/title";
  private static final String DC_DESCRIPTION = "http://purl.org/dc/terms/description";
  private static final String DC_CREATOR = "http://purl.org/dc/terms/creator";
  private static final String DC_DATE = "http://purl.org/dc/terms/date";
  private static final Model defaultModel = ModelFactory.createDefaultModel();

  private static final String esPrefix = "http://drugis.org/eventSourcing/es#";
  public static final String REVISION = "revision";
  public static final String VERSION = "version";
  public static final String GRAPH = "graph";
  public static final Property TYPE_PROPERTY = defaultModel.getProperty(RDF_TYPE_URI);
  public static final Property DESCRIPTION_PROPERTY = defaultModel.getProperty(DC_DESCRIPTION);
  public static final Property DATE_PROPERTY = defaultModel.getProperty(DC_DATE);
  public static final Property TITLE_PROPERTY = defaultModel.getProperty(DC_TITLE);
  public static final Property creatorProperty = defaultModel.getProperty(DC_CREATOR);
  public static final Property headVersionProperty = defaultModel.getProperty(esPrefix, HEAD);
  public static final Property datasetVersionObject = defaultModel.getProperty(esPrefix, DATASET_VERSION);
  public static final Property previousProperty = defaultModel.getProperty(esPrefix, PREVIOUS);
  public static final Property mergedRevisionProperty = defaultModel.getProperty(esPrefix, MERGED_REVISION);
  public static final Property graphRevisionProperty = defaultModel.getProperty(esPrefix, GRAPH_REVISION);
  public static final Property revisionProperty = defaultModel.getProperty(esPrefix, REVISION);
  public static final Property graphProperty = defaultModel.getProperty(esPrefix, GRAPH);
  public static final Property datasetProperty = defaultModel.getProperty(esPrefix, DATASET);

}
