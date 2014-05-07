define([], function() {
  var dependencies = ['$scope', '$q', '$stateParams', 'OutcomeResource'];

  var NetworkMetaAnalysisController = function($scope, $q, $stateParams, OutcomeResource) {
    $scope.analysis = $scope.$parent.analysis;
    $scope.project = $scope.$parent.project;
    $scope.outcomes = OutcomeResource.query({
      projectId: $stateParams.projectId
    });
    $scope.selectedOutcome = {};

    $q.all($scope.analysis, $scope.project, $scope.outcomes).then(function() {
      $scope.$watch('selectedOutcome', function(newValue, oldValue) {
        if (newValue !== oldValue) {
          $scope.analysis.$save();
        }
      });
    });
  };

  return dependencies.concat(NetworkMetaAnalysisController);
});