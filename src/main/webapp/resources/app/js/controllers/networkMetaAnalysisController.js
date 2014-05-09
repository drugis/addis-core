define([], function() {
  var dependencies = ['$scope', '$q', '$stateParams', 'OutcomeResource', 'TrialverseTrialDataResource'];

  var NetworkMetaAnalysisController = function($scope, $q, $stateParams, OutcomeResource, TrialverseTrialDataResource) {
    $scope.analysis = $scope.$parent.analysis;
    $scope.project = $scope.$parent.project;
    $scope.outcomes = OutcomeResource.query({
      projectId: $stateParams.projectId
    });
    $scope.trialData;

    function matchOutcome(outcome) {
      return $scope.analysis.outcome.id === outcome.id;
    }

    $q.all([$scope.analysis.$promise, $scope.project.$promise, $scope.outcomes.$promise]).then(function() {
      $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);

      if ($scope.analysis.outcome) {
        $scope.trialData = TrialverseTrialDataResource.get({
          id: $scope.project.trialverseId,
          outcomeUri: $scope.analysis.outcome.semanticOutcomeUri
        })
      }
    });

    $scope.saveAnalysis = function() {
      $scope.analysis.$save(function() {
        $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);
        $scope.trialData = TrialverseTrialDataResource.get({
          id: $scope.project.trialverseId,
          outcomeUri: $scope.analysis.outcome.semanticOutcomeUri
        })
      });
    }
  };

  return dependencies.concat(NetworkMetaAnalysisController);
});