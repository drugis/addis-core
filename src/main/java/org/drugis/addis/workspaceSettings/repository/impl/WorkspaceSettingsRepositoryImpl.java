package org.drugis.addis.workspaceSettings.repository.impl;

import org.drugis.addis.workspaceSettings.WorkspaceSettings;
import org.drugis.addis.workspaceSettings.repository.WorkspaceSettingsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
@Transactional
public class WorkspaceSettingsRepositoryImpl implements WorkspaceSettingsRepository {
  @Qualifier("emAddisCore")
  @PersistenceContext(unitName = "addisCore")
  private EntityManager em;

  @Override
  public WorkspaceSettings get(Integer analysisId) {
    WorkspaceSettings workspaceSettings = em.find(WorkspaceSettings.class, analysisId);
    if (workspaceSettings == null) {
      workspaceSettings = new WorkspaceSettings();
    }
    return workspaceSettings;
  }

  @Override
  public void put(Integer analysisId, String settings) {
    WorkspaceSettings oldWorkspaceSettings = get(analysisId);
    WorkspaceSettings newSettings = new WorkspaceSettings(analysisId, settings);
    if (oldWorkspaceSettings == null) {
      em.persist(newSettings);
    } else {
      em.merge(newSettings);
    }
  }

}
