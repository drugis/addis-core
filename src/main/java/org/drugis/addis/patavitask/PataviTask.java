package org.drugis.addis.patavitask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;

/**
 * Created by connor on 26-6-14.
 */
public class PataviTask {

  private String status;

  private URI self;

  private URI results;

  private URI updates;

  public PataviTask() {
  }

  public PataviTask(String pataviResponse) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode = objectMapper.readTree(pataviResponse);
    this.status = jsonNode.get("status").asText();
    this.self = URI.create(jsonNode.get("_links").get("self").get("href").asText());
    this.updates = URI.create(jsonNode.get("_links").get("updates").get("href").asText());
    if (jsonNode.get("_links").get("results") != null) {
      this.results = URI.create(jsonNode.get("_links").get("results").get("href").asText());
    }
  }

  public String getStatus() {
    return status;
  }

  public URI getSelf() {
    return self;
  }

  public URI getResults() {
    return results;
  }

  public URI getUpdates() {
    return updates;
  }

  public Boolean hasResults() {
    return this.results != null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    PataviTask that = (PataviTask) o;

    if (!status.equals(that.status)) return false;
    if (!self.equals(that.self)) return false;
    if (results != null ? !results.equals(that.results) : that.results != null) return false;
    return updates.equals(that.updates);

  }

  @Override
  public int hashCode() {
    int result = status.hashCode();
    result = 31 * result + self.hashCode();
    result = 31 * result + (results != null ? results.hashCode() : 0);
    result = 31 * result + updates.hashCode();
    return result;
  }
}
