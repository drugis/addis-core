'use strict';
define(['underscore'], function () {
  var dependencies = ['$scope', '$stateParams', '$q',
    'ProjectsService', 'AnalysisService', 'OutcomeService', 'Select2UtilService'
  ];
  var AnalysisController = function ($scope, $stateParams, $q,
    ProjectsService, AnalysisService, OutcomeService, Select2UtilService) {
    $scope.loading = {
      loaded: false
    };
    $scope.project = ProjectsService.get($stateParams);
    $scope.analysis = AnalysisService.get($stateParams);
    $scope.outcomes = OutcomeService.query($stateParams);
    $scope.selectedOutcomeIds = [];

    $q.all([$scope.project.$promise, $scope.analysis.$promise]).then(function () {
      $scope.loading.loaded = true;
      $scope.selectedOutcomeIds = Select2UtilService.objectsToIds($scope.analysis.selectedOutcomes);
      $scope.$watchCollection('selectedOutcomeIds', function () {
        $scope.analysis.selectedOutcomes = Select2UtilService.idsToObjects($scope.selectedOutcomeIds, $scope.outcomes);
        $scope.analysis.$save();
      });
    });


  }
  return dependencies.concat(AnalysisController);
});