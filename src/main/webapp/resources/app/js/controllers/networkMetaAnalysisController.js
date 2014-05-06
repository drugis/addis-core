define([], function() {
  var dependencies = ['$scope', '$stateParams', 'OutcomeResource'
  ];

  var NetworkMetaAnalysisController = function($scope, $stateParams, OutcomeResource) {
    $scope.analysis = $scope.$parent.analysis;
    $scope.project = $scope.$parent.project;
    $scope.outcomes = OutcomeResource.query({projectId: $stateParams.projectId});
    $scope.selectedOutcome = {};
  };

  return dependencies.concat(NetworkMetaAnalysisController);
});