'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', '$q', 'ProjectsService', 'AnalysisService', 'OutcomeService'];
  var AnalysisController = function($scope, $stateParams, $q, ProjectsService, AnalysisService, OutcomeService) {
    $scope.loading = {loaded : false};
    $scope.project = ProjectsService.get($stateParams);
    $scope.analysis = AnalysisService.get($stateParams);
     $scope.outcomes = OutcomeService.query($stateParams);

    $q.all([$scope.project.$promise, $scope.analysis.$promise]).then(function () {
      $scope.loading.loaded = true;
    });

  }
  return dependencies.concat(AnalysisController);
});