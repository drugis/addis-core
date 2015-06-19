package org.drugis.addis.patavitask;

import org.drugis.addis.patavitask.repository.PataviTaskRepository;
import org.drugis.addis.patavitask.repository.impl.PataviTaskRepositoryImpl;
import org.drugis.addis.patavitask.repository.impl.SimpleJdbcInsertPataviTaskFactory;
import org.drugis.addis.problems.model.AbstractNetworkMetaAnalysisProblemEntry;
import org.drugis.addis.problems.model.NetworkMetaAnalysisProblem;
import org.drugis.addis.problems.model.RateNetworkMetaAnalysisProblemEntry;
import org.drugis.addis.problems.model.TreatmentEntry;
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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
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
  public void testCreate() throws Exception {
    Integer analysisId = -5; // from test-data/sql

    Long responders = 1L;
    Long samplesize = 30L;
    Integer treatment = 123;
    String study = "study";
    List<AbstractNetworkMetaAnalysisProblemEntry> entries = new ArrayList<>();
    entries.add(new RateNetworkMetaAnalysisProblemEntry(study, treatment, samplesize, responders));
    String enrtyName = "entry name";
    Integer entryId = 456;
    List<TreatmentEntry> treatments = Arrays.asList(new TreatmentEntry(entryId, enrtyName));
    NetworkMetaAnalysisProblem problem = new NetworkMetaAnalysisProblem(entries, treatments);

    PataviTask task = pataviTaskRepository.createPataviTask(problem);
    assertNotNull(task.getId());

  }


}
