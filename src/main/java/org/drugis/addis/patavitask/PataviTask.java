package org.drugis.addis.patavitask;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;

/**
 * Created by connor on 26-6-14.
 */
public class PataviTask {

  private String id;

  private String service;

  private String status;

  private URI self;

  private URI results;

  private URI updates;

  public PataviTask() {
  }

  public PataviTask(String pataviResponse) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode = objectMapper.readTree(pataviResponse);
    this.id = jsonNode.get("id").asText();
    this.service = jsonNode.get("service").asText();
    this.status = jsonNode.get("status").asText();
    this.self = URI.create(jsonNode.get("_link").get("self").get("href").asText());
    this.updates = URI.create(jsonNode.get("_link").get("updates").get("href").asText());
    if (jsonNode.get("_link").get("results") != null) {
      this.results = URI.create(jsonNode.get("_link").get("results").get("href").asText());
    }
  }

  public String getId() {
    return id;
  }

  public String getService() {
    return service;
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

    if (!id.equals(that.id)) return false;
    if (!service.equals(that.service)) return false;
    if (!status.equals(that.status)) return false;
    if (!self.equals(that.self)) return false;
    if (results != null ? !results.equals(that.results) : that.results != null) return false;
    return updates.equals(that.updates);

  }

  @Override
  public int hashCode() {
    int result = id.hashCode();
    result = 31 * result + service.hashCode();
    result = 31 * result + status.hashCode();
    result = 31 * result + self.hashCode();
    result = 31 * result + (results != null ? results.hashCode() : 0);
    result = 31 * result + updates.hashCode();
    return result;
  }
}
