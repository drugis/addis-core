'use strict';
define([], function() {
  var dependencies = ['$scope', 'currentAnalysis', 'currentProject', 'currentSchemaVersion', 
    'UserService', 'SchemaService'];
  var AbstractBenefitRiskController = function($scope, currentAnalysis, currentProject, 
    currentSchemaVersion, UserService, SchemaService) {
    if(currentAnalysis.problem.schemaVersion !== currentSchemaVersion){
      $scope.workspace = SchemaService.updateWorkspaceToCurrentSchema(currentAnalysis);
    } else {
      $scope.workspace = currentAnalysis;
    }

    $scope.project = currentProject;
    $scope.editMode = {};
    UserService.isLoginUserId(currentProject.owner.id).then(function(isLoginUser) {
      $scope.editMode.isUserOwner = isLoginUser;
    });
  };
  return dependencies.concat(AbstractBenefitRiskController);
});
