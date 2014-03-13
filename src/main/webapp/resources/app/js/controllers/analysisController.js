'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', '$q', 'ProjectsService', 'AnalysisService'];
  var AnalysisController = function($scope, $stateParams, $q, ProjectsService, AnalysisService) {
    $scope.loading = {loaded : false};
    $scope.project = ProjectsService.get($stateParams);
    $scope.analysis = AnalysisService.get($stateParams);

    $q.all([$scope.project.$promise, $scope.analysis.$promise]).then(function () {
      $scope.loading.loaded = true;
    });

    $scope.outcomes = [{id: 1, label: 'crit1'}, {id: 2, label: 'crit2 with a long long long loong name'}, {id: 3, label: 'crit3'}, {id: 4, label: 'crit4'}, {id: 5, label: 'crit5'}];
  }
  return dependencies.concat(AnalysisController);
});