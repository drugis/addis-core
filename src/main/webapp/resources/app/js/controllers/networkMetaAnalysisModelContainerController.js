'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', 'currentAnalysis', 'currentProject'];

  var NetworkMetaAnalysisModelContainerController = function($scope, $stateParams, currentAnalysis, currentProject) {
       $scope.project = currentProject;
       $scope.analysis = currentAnalysis;
  };

  return dependencies.concat(NetworkMetaAnalysisModelContainerController);
});
