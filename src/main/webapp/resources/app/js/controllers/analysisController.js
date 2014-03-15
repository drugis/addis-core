'use strict';
define(['underscore'], function() {
  var dependencies = ['$scope', '$stateParams', '$q', 'ProjectsService', 'AnalysisService', 'OutcomeService'];
  var AnalysisController = function($scope, $stateParams, $q,
                                    ProjectsService, AnalysisService, OutcomeService) {
    $scope.loading = {loaded : false};
    $scope.project = ProjectsService.get($stateParams);
    $scope.analysis = AnalysisService.get($stateParams);
    $scope.outcomes = OutcomeService.query($stateParams);
    $scope.selectedOutcomeIds = [];

    var idsToOutcomes = function(selectedOutcomeIds, outcomes) {
      return _.map(selectedOutcomeIds, function(outcomeId) {
        return _.find(outcomes, function(outcome) {
          return outcome && outcome.id === parseInt(outcomeId, 10);
        });
      });
    };

    var outcomesToIds = function(outcomes) {
      return _.map(outcomes, function(outcome) {
        return outcome.id.toString();
      });
    };

    $q.all([$scope.project.$promise, $scope.analysis.$promise]).then(function () {
      $scope.loading.loaded = true;
      $scope.selectedOutcomeIds = outcomesToIds($scope.analysis.selectedOutcomes);
      $scope.$watchCollection('selectedOutcomeIds', function() {
        $scope.analysis.selectedOutcomes = idsToOutcomes($scope.selectedOutcomeIds, $scope.outcomes);
        $scope.analysis.$save();
      });
    });


  }
  return dependencies.concat(AnalysisController);
});