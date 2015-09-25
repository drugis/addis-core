package org.drugis.addis.patavitask;

import com.jayway.jsonpath.InvalidPathException;
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
import java.util.Collections;
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
    when(mockSimpleJdbcInsert.executeAndReturnKey(any(MapSqlParameterSource.class))).thenReturn(123);
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
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;
    Model model = new Model.ModelBuilder()
            .analysisId(analysisId)
            .title("title")
            .linearModel(linearModel)
            .modelType("network")
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor)
            .likelihood(likelihood)
            .link(link)
            .build();

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();
    entries.add(new RateNetworkMetaAnalysisProblemEntry(study, treatment, samplesize, responders));
    String entryName = "entry name";
    Integer entryId = 456;
    List<TreatmentEntry> treatments = Collections.singletonList(new TreatmentEntry(entryId, entryName));
    NetworkMetaAnalysisProblem problem = new NetworkMetaAnalysisProblem(entries, treatments);

    PataviTask task = pataviTaskRepository.createPataviTask(problem, model);
    assertNotNull(task.getId());
    JSONObject parsedProblem = new JSONObject(task.getProblem());
    assertEquals(linearModel, parsedProblem.get("linearModel"));
    assertEquals("network", JsonPath.read(task.getProblem(), "$.modelType.type"));
    assertEquals(burnInIterations, JsonPath.read(task.getProblem(), "$.burnInIterations"));
    assertEquals(inferenceIterations, JsonPath.read(task.getProblem(), "$.inferenceIterations"));
    assertEquals(thinningFactor, JsonPath.read(task.getProblem(), "$.thinningFactor"));
    assertEquals(likelihood, JsonPath.read(task.getProblem(), "$.likelihood"));
    assertEquals(link, JsonPath.read(task.getProblem(), "$.link"));
  }

  @Test
  public void testCreatePairwise() throws Exception, InvalidModelTypeException {
    Integer analysisId = -5; // from test-data/sql

    Long responders = 1L;
    Long samplesize = 30L;
    Integer treatment = 123;
    String study = "study";
    String linearModel = "random";
    String fromTreatment = "fromTreatment";
    String toTreatment = "toTreatment";
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;
    Model model = new Model.ModelBuilder()
            .analysisId(analysisId)
            .title("title")
            .linearModel(linearModel)
            .modelType("pairwise")
            .from(new Model.DetailNode(-1, fromTreatment))
            .to(new Model.DetailNode(-2, toTreatment))
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor)
            .likelihood(likelihood)
            .link(link)
            .build();

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();
    entries.add(new RateNetworkMetaAnalysisProblemEntry(study, treatment, samplesize, responders));
    String entryName = "entry name";
    Integer entryId = 456;
    List<TreatmentEntry> treatments = Collections.singletonList(new TreatmentEntry(entryId, entryName));
    NetworkMetaAnalysisProblem problem = new NetworkMetaAnalysisProblem(entries, treatments);

    PataviTask task = pataviTaskRepository.createPataviTask(problem, model);
    assertNotNull(task.getId());
    JSONObject parsedProblem = new JSONObject(task.getProblem());
    assertEquals(linearModel, parsedProblem.get("linearModel"));
    assertEquals("pairwise", JsonPath.read(task.getProblem(), "$.modelType.type"));
    assertEquals(fromTreatment, JsonPath.read(task.getProblem(), "$.modelType.details.from.name"));
    assertEquals(toTreatment, JsonPath.read(task.getProblem(), "$.modelType.details.to.name"));
    assertEquals(likelihood, JsonPath.read(task.getProblem(), "$.likelihood"));
    assertEquals(link, JsonPath.read(task.getProblem(), "$.link"));
  }

  @Test
  public void testCreateWithFixedOutcomeScale() throws Exception, InvalidModelTypeException {
    Integer analysisId = -5; // from test-data/sql

    Long responders = 1L;
    Long samplesize = 30L;
    Integer treatment = 123;
    String study = "study";
    String linearModel = "random";
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;
    Double outcomeScale = 2.2;
    Model model = new Model.ModelBuilder()
            .analysisId(analysisId)
            .title("title")
            .linearModel(linearModel)
            .modelType("network")
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor)
            .likelihood(likelihood)
            .link(link)
            .outcomeScale(outcomeScale)
            .build();

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();
    entries.add(new RateNetworkMetaAnalysisProblemEntry(study, treatment, samplesize, responders));
    String entryName = "entry name";
    Integer entryId = 456;
    List<TreatmentEntry> treatments = Collections.singletonList(new TreatmentEntry(entryId, entryName));
    NetworkMetaAnalysisProblem problem = new NetworkMetaAnalysisProblem(entries, treatments);

    PataviTask task = pataviTaskRepository.createPataviTask(problem, model);
    assertEquals(outcomeScale, JsonPath.read(task.getProblem(), "$.outcomeScale"));
  }

  @Test(expected = InvalidPathException.class)
  public void testCreateWithoutFixedOutcomeScale() throws Exception, InvalidModelTypeException {
    Integer analysisId = -5; // from test-data/sql

    Long responders = 1L;
    Long samplesize = 30L;
    Integer treatment = 123;
    String study = "study";
    String linearModel = "random";
    Integer burnInIterations = 5000;
    Integer inferenceIterations = 20000;
    Integer thinningFactor = 10;
    String likelihood = Model.LIKELIHOOD_BINOM;
    String link = Model.LINK_LOG;
    Model model = new Model.ModelBuilder()
            .analysisId(analysisId)
            .title("title")
            .linearModel(linearModel)
            .modelType("network")
            .burnInIterations(burnInIterations)
            .inferenceIterations(inferenceIterations)
            .thinningFactor(thinningFactor)
            .likelihood(likelihood)
            .link(link)
            .build();

    List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();
    entries.add(new RateNetworkMetaAnalysisProblemEntry(study, treatment, samplesize, responders));
    String entryName = "entry name";
    Integer entryId = 456;
    List<TreatmentEntry> treatments = Collections.singletonList(new TreatmentEntry(entryId, entryName));
    NetworkMetaAnalysisProblem problem = new NetworkMetaAnalysisProblem(entries, treatments);

    PataviTask task = pataviTaskRepository.createPataviTask(problem, model);
    JsonPath.read(task.getProblem(), "$.outcomeScale");
  }


}
