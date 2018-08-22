package org.drugis.addis.workspaceSettings.repository;

import org.drugis.addis.workspaceSettings.WorkspaceSettings;

public interface WorkspaceSettingsRepository {
  WorkspaceSettings get(Integer analysisId);

  void put(Integer analysisId, String settings);
}
