'use strict';
define([], function() {
  var dependencies = ['$scope', 'currentAnalysis', 'currentProject'];
  var BenefitRiskController = function($scope, currentAnalysis, currentProject) {
    $scope.workspace = currentAnalysis;
    $scope.project = currentProject;
    $scope.editMode = {
      isUserOwner: true
    };
  };
  return dependencies.concat(BenefitRiskController);
});
