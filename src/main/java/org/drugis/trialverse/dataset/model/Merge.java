package org.drugis.trialverse.dataset.model;

/**
 * Created by daan on 2-9-15.
 */
public class Merge {
  private String mergedRevisionUri;
  private String sourceDatasetUri;
  private String version;
  private String graph;
  private String title;

  public Merge() {
  }

  public Merge(String mergedRevisionUri, String sourceDatasetUri, String version, String graph, String title) {
    this.version = version;
    this.graph = graph;
    this.title = title;
    this.mergedRevisionUri = mergedRevisionUri;
    this.sourceDatasetUri = sourceDatasetUri;
  }

  public String getMergedRevisionUri() {
    return mergedRevisionUri;
  }

  public String getSourceDatasetUri() {
    return sourceDatasetUri;
  }

  public String getVersion() {
    return version;
  }

  public String getGraph() {
    return graph;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Merge merge = (Merge) o;

    if (!mergedRevisionUri.equals(merge.mergedRevisionUri)) return false;
    if (!sourceDatasetUri.equals(merge.sourceDatasetUri)) return false;
    if (!version.equals(merge.version)) return false;
    if (!graph.equals(merge.graph)) return false;
    return title.equals(merge.title);

  }

  @Override
  public int hashCode() {
    int result = mergedRevisionUri.hashCode();
    result = 31 * result + sourceDatasetUri.hashCode();
    result = 31 * result + version.hashCode();
    result = 31 * result + graph.hashCode();
    result = 31 * result + title.hashCode();
    return result;
  }
}
