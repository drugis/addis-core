'use strict';
define([], function() {
  var dependencies = [
    '$scope',
    'currentAnalysis',
    'currentProject',
    'currentSchemaVersion',
    'UserService',
    'SchemaService',
    'WorkspaceSettingsService'
  ];
  var AbstractBenefitRiskController = function(
    $scope,
    currentAnalysis,
    currentProject,
    currentSchemaVersion,
    UserService,
    SchemaService,
    WorkspaceSettingsService
  ) {
    if (currentAnalysis.problem.schemaVersion !== currentSchemaVersion) {
      $scope.workspace = SchemaService.updateWorkspaceToCurrentSchema(currentAnalysis);
    } else {
      $scope.workspace = currentAnalysis;
    }

    $scope.project = currentProject;
    $scope.editMode = {};
    UserService.isLoginUserId(currentProject.owner.id).then(function(isLoginUser) {
      $scope.editMode.isUserOwner = isLoginUser;
    });

    getWorkspaceSettings();
    $scope.$on('elicit.settingsChanged', getWorkspaceSettings);
    
    function getWorkspaceSettings() {
      $scope.toggledColumns = WorkspaceSettingsService.getToggledColumns();
      $scope.workspaceSettings = WorkspaceSettingsService.getWorkspaceSettings();
    }

  };
  return dependencies.concat(AbstractBenefitRiskController);
});
