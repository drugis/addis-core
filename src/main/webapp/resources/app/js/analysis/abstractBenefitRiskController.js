'use strict';
define([], function() {
  var dependencies = ['$scope', 'currentAnalysis', 'currentProject', 'UserService', 'SchemaService'];
  var AbstractBenefitRiskController = function($scope, currentAnalysis, currentProject, UserService, SchemaService) {
    if(currentAnalysis.problem.schemaVersion !== currentSchemaVersion){
      $scope.workspace = SchemaService.updateWorkspaceToCurrentSchema(currentAnalysis);
    } else {
      $scope.workspace = currentAnalysis;
    }

    $scope.project = currentProject;
    var isUserOwner = UserService.isLoginUserId(currentProject.owner.id);
    $scope.editMode = {
      isUserOwner: isUserOwner
    };
  };
  return dependencies.concat(AbstractBenefitRiskController);
});
