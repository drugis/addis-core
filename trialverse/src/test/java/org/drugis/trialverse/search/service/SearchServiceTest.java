package org.drugis.trialverse.search.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.drugis.trialverse.dataset.model.VersionMapping;
import org.drugis.trialverse.dataset.repository.DatasetReadRepository;
import org.drugis.trialverse.dataset.repository.VersionMappingRepository;
import org.drugis.trialverse.search.model.SearchResult;
import org.drugis.trialverse.search.service.impl.SearchServiceImpl;
import org.drugis.trialverse.util.WebConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;


import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Created by connor on 9/4/15.
 */
public class SearchServiceTest {

  @Mock
  VersionMappingRepository versionMappingRepository;

  @Mock
  DatasetReadRepository datasetReadRepository;

  @InjectMocks
  SearchService searchService;

  @Before
  public void init() {
    searchService = new SearchServiceImpl();
    initMocks(this);
  }

  @Test
  public void testSearchStudy() throws URISyntaxException, IOException, ParseException {
    String trialverseDatasetUrl = "trialverseDatasetUrl";
    String versionedDatasetUrl = "versiondDatasetUrl";
    VersionMapping versionMapping = new VersionMapping(versionedDatasetUrl, "ownerUid", trialverseDatasetUrl);
    List<VersionMapping> versionMappings = Arrays.asList(versionMapping);
    when(versionMappingRepository.getVersionMappings()).thenReturn(versionMappings);

    String searchQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
            "prefix ontology: <http://trials.drugis.org/ontology#>\n" +
            "\n" +
            "SELECT ?graph ?study ?label ?comment WHERE {\n" +
            "  graph ?graph {\n" +
            "    {\n" +
            "      ?study\n" +
            "        a ontology:Study ;\n" +
            "        rdfs:label ?labelSearchResult .\n" +
            "        FILTER regex(?labelSearchResult, \"my term\", \"i\") .\n" +
            "    } UNION {\n" +
            "      ?study\n" +
            "        a ontology:Study ;\n" +
            "        rdfs:comment ?commentSearchResult\n" +
            "      FILTER regex(?commentSearchResult, \"my term\", \"i\")\n" +
            "    }.\n" +
            "    ?study rdfs:label ?label .\n" +
            "    OPTIONAL {?study rdfs:comment ?comment .}\n" +
            "  }\n" +
            "}";
    InputStream inputStream = new ClassPathResource("mockSearchResults.json").getInputStream();
    JSONObject result = (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(inputStream);
    result.put(WebConstants.VERSION_UUID, "versionUri");

    when(datasetReadRepository.executeHeadQuery(searchQuery, versionMapping)).thenReturn(result);

    List<SearchResult> searchResults = searchService.searchStudy("my term");
    assertEquals(2, searchResults.size());
  }



}

