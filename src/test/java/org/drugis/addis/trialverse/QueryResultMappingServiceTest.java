package org.drugis.addis.trialverse;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;
import org.drugis.addis.TestUtils;
import org.drugis.addis.trialverse.model.TrialDataStudy;
import org.drugis.addis.trialverse.service.QueryResultMappingService;
import org.drugis.addis.trialverse.service.impl.QueryResultMappingServiceImpl;
import org.drugis.addis.trialverse.service.impl.ReadValueException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by connor on 8-4-16.
 */
public class QueryResultMappingServiceTest {
  @InjectMocks
  QueryResultMappingService queryResultMappingService;

  String resultRows = TestUtils.loadResource(this.getClass(), "/triplestoreService/trialDataEdarbiReultRowsExample.json");

  @Before
  public void setUp() {
    queryResultMappingService = new QueryResultMappingServiceImpl();
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testMapResultRowToTrialDataStudy() throws ParseException, ReadValueException {
    JSONArray bindings = (JSONArray) JSONValue.parseWithException(resultRows);
    Map<String, TrialDataStudy> trialDataMap = queryResultMappingService.mapResultRowToTrialDataStudy(bindings);
    assertEquals(5, trialDataMap.size());
  }
}
