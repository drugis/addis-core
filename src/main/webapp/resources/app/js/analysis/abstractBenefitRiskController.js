'use strict';
define([], function() {
  var dependencies = ['$scope', 'currentAnalysis', 'currentProject', 'UserService'];
  var AbstractBenefitRiskController = function($scope, currentAnalysis, currentProject, UserService) {
    $scope.workspace = currentAnalysis;
    $scope.project = currentProject;
    $scope.editMode = {};
    UserService.isLoginUserId(currentProject.owner.id).then(function(isLoginUser) {
      $scope.editMode.isUserOwner = isLoginUser;
    });
  };
  return dependencies.concat(AbstractBenefitRiskController);
});
