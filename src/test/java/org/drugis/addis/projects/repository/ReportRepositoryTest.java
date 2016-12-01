package org.drugis.addis.projects.repository;

import org.drugis.addis.config.JpaRepositoryTestConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static org.junit.Assert.*;

/**
 * Created by daan on 23-11-16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(classes = {JpaRepositoryTestConfig.class})
public class ReportRepositoryTest {

  @Inject
  private ReportRepository reportRepository;
  private final Integer projectId = 1;
  private final String oldReport = "this is a report";

  @Test
  public void testGetReport() {
    String result = reportRepository.get(projectId);
    assertEquals(oldReport, result);
  }

  @Test
  public void testUpdateNew() {
    int newProject = 2;
    String newReport = "it's a new report";
    reportRepository.update(newProject, newReport);
    String oldResult = reportRepository.get(projectId);
    String newResult = reportRepository.get(newProject);
    assertEquals(oldReport, oldResult);
    assertEquals(newReport, newResult);
  }

  @Test
  public void testUpdateOld() {
    String newReport = "whoah new text";
    reportRepository.update(projectId, newReport);
    String newResult = reportRepository.get(projectId);
    assertEquals(newReport, newResult);
  }

  @Test
  public void testDeleteReport() {
    //make sure text is not on default already
    String newReport = "whoah new text";
    reportRepository.update(projectId, newReport);
    String newResult = reportRepository.get(projectId);
    assertEquals(newReport, newResult);
    reportRepository.delete(projectId);
    newResult = reportRepository.get(projectId);
    assertEquals("default report text",newResult);
  }

}