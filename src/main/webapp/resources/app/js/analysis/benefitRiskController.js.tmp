'use strict';
define([], function() {
  var dependencies = ['$scope', 'currentAnalysis', 'currentProject', 'UserService'];
  var BenefitRiskController = function($scope, currentAnalysis, currentProject, UserService) {
    $scope.workspace = currentAnalysis;
    $scope.project = currentProject;
    var isUserOwner = UserService.isLoginUserId(currentProject.owner.id);
    $scope.editMode = {
      isUserOwner: isUserOwner
    };
  };
  return dependencies.concat(BenefitRiskController);
});
