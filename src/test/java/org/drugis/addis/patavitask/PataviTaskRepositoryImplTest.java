package org.drugis.addis.patavitask;

import com.jayway.jsonpath.JsonPath;
import org.drugis.addis.models.Model;
import org.drugis.addis.models.exceptions.InvalidModelTypeException;
import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.repository.impl.PataviTaskRepositoryImpl;
import org.drugis.addis.patavitask.repository.impl.SimpleJdbcInsertPataviTaskFactory;
import org.drugis.addis.problems.model.AbstractNetworkMetaAnalysisProblemEntry;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;
import org.drugis.addis.problems.model.RateNetworkMetaAnalysisProblemEntry;
import org.drugis.addis.problems.model.TreatmentEntry;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PataviTaskRepositoryImplTest {

  @Inject
  JdbcTemplate jdbcTemplate;

  @Inject
  SimpleJdbcInsertPataviTaskFactory simpleJdbcInsertPataviTaskFactory;

  @InjectMocks
  private PataviTaskRepository pataviTaskRepository;

  @Before
  public void setUp() {
    jdbcTemplate = mock(JdbcTemplate.class);
    DataSource datasource = mock(DataSource.class);
    when(jdbcTemplate.getDataSource()).thenReturn(datasource);
    simpleJdbcInsertPataviTaskFactory = mock(SimpleJdbcInsertPataviTaskFactory.class);
    SimpleJdbcInsert mockSimpleJdbcInsert = mock(SimpleJdbcInsert.class);
    when(mockSimpleJdbcInsert.executeAndReturnKey(any(MapSqlParameterSource.class))).thenReturn(new Integer(123));
    when(simpleJdbcInsertPataviTaskFactory.build(jdbcTemplate)).thenReturn(mockSimpleJdbcInsert);
    pataviTaskRepository = new PataviTaskRepositoryImpl();
    initMocks(this);
  }

  @Test
  public void testCreateNetwork() throws Exception, InvalidModelTypeException {
    Integer analysisId = -5; // from test-data/sql

    Long responders = 1L;
    Long samplesize = 30L;
    Integer treatment = 123;
    String study = "study";
    String linearModel = "random";
    Model model = new Model(analysisId, "title", linearModel, "network", null, null);

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();
    entries.add(new RateNetworkMetaAnalysisProblemEntry(study, treatment, samplesize, responders));
    String entryName = "entry name";
    Integer entryId = 456;
    List<TreatmentEntry> treatments = Arrays.asList(new TreatmentEntry(entryId, entryName));
    NetworkMetaAnalysisProblem problem = new NetworkMetaAnalysisProblem(entries, treatments);

    PataviTask task = pataviTaskRepository.createPataviTask(problem, model);
    assertNotNull(task.getId());
    JSONObject parsedProblem = new JSONObject(task.getProblem());
    assertEquals(linearModel, parsedProblem.get("linearModel"));
    assertEquals("network", JsonPath.read(task.getProblem(), "$.modelType.type"));
  }

  @Test
  public void testCreatePairwise() throws Exception, InvalidModelTypeException {
    Integer analysisId = -5; // from test-data/sql

    Long responders = 1L;
    Long samplesize = 30L;
    Integer treatment = 123;
    String study = "study";
    String linearModel = "random";
    String treatment1 = "treatment 1";
    String treatment2 = "treatment 2";
    Model model = new Model(analysisId, "title", linearModel, "pairwise", treatment1, treatment2);

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();
    entries.add(new RateNetworkMetaAnalysisProblemEntry(study, treatment, samplesize, responders));
    String entryName = "entry name";
    Integer entryId = 456;
    List<TreatmentEntry> treatments = Arrays.asList(new TreatmentEntry(entryId, entryName));
    NetworkMetaAnalysisProblem problem = new NetworkMetaAnalysisProblem(entries, treatments);

    PataviTask task = pataviTaskRepository.createPataviTask(problem, model);
    assertNotNull(task.getId());
    JSONObject parsedProblem = new JSONObject(task.getProblem());
    assertEquals(linearModel, parsedProblem.get("linearModel"));
    assertEquals("pairwise", JsonPath.read(task.getProblem(), "$.modelType.type"));
    assertEquals(treatment1, JsonPath.read(task.getProblem(), "$.modelType.details.from"));
    assertEquals(treatment2, JsonPath.read(task.getProblem(), "$.modelType.details.to"));
  }


}
