define([], function() {
  var dependencies = ['$scope', '$q', '$stateParams', 'OutcomeResource'];

  var NetworkMetaAnalysisController = function($scope, $q, $stateParams, OutcomeResource) {
    $scope.analysis = $scope.$parent.analysis;
    $scope.project = $scope.$parent.project;
    $scope.outcomes = OutcomeResource.query({
      projectId: $stateParams.projectId
    });

    function matchOutcome(outcome) {
      return $scope.analysis.outcome.id === outcome.id;
    }

    $q.all([$scope.analysis.$promise, $scope.project.$promise, $scope.outcomes.$promise]).then(function() {
      $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);
    });

    $scope.saveAnalysis = function() {
      $scope.analysis.$save(function() {
        $scope.analysis.outcome =  _.find($scope.outcomes, matchOutcome);
      });
    }
  };

  return dependencies.concat(NetworkMetaAnalysisController);
});