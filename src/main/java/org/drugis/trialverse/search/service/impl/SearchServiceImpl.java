package org.drugis.trialverse.search.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import org.apache.commons.compress.utils.IOUtils;
import org.drugis.addis.security.Account;
import org.drugis.addis.security.repository.AccountRepository;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.search.model.SearchResult;
import org.drugis.trialverse.search.service.SearchService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by connor on 01/09/15.
 */
@Service
public class SearchServiceImpl implements SearchService {

  private static final String FIND_STUDIES_BY_TERMS_SPARQL = "findStudiesByTerms.sparql";
  private static final String SEARCH_QUERY_TEMPLATE = readQueryTemplate();

  @Inject
  VersionMappingRepository versionMappingRepository;

  @Inject
  DatasetReadRepository datasetReadRepository;

  @Inject
  AccountRepository accountRepository;


  private static String readQueryTemplate() {
    try {
      return new String(new ClassPathResource(FIND_STUDIES_BY_TERMS_SPARQL).getInputStream().readAllBytes());
    } catch (IOException e) {
      throw new RuntimeException("could not load find queryTemplate with name " + FIND_STUDIES_BY_TERMS_SPARQL);
    }
  }


  @Override
  public List<SearchResult> searchStudy(String searchTerm) throws IOException, URISyntaxException {
    List<VersionMapping> mappings = versionMappingRepository.getVersionMappings();
    String queryString = SEARCH_QUERY_TEMPLATE.replace("$searchTerm", searchTerm);
    List<SearchResult> aggregateResults = new ArrayList<>();
    for (VersionMapping mapping : mappings) {
      Account account = accountRepository.findAccountByEmail(mapping.getOwnerUuid());
      JSONObject queryResult = datasetReadRepository.executeHeadQuery(queryString, mapping);
      Object result =  new ObjectMapper().readValue(queryResult.toJSONString(), SearchResult.class);
      List<SearchResult> searchResults = (ArrayList<SearchResult>) result;
      searchResults.stream().forEach((streamResult) -> streamResult.addMetaData(mapping, account));
      System.out.println(queryResult);
      aggregateResults.addAll(searchResults);
    }
    return aggregateResults;
  }
}
