'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', 'ProjectsService', 'TrialverseService', 'SemanticOutcomeService', 'OutcomeService'];
  var ProjectsController = function($scope, $stateParams, ProjectsService, TrialverseService, SemanticOutcomeService, OutcomeService) {

    $scope.loading = {loaded : false};
    $scope.project = ProjectsService.get($stateParams);

    $scope.project.$promise.then(function() {
      $scope.trialverse = TrialverseService.get({id: $scope.project.trialverseId});
      $scope.semanticOutcomes = SemanticOutcomeService.query({id: $scope.project.trialverseId});
      $scope.loading.loaded = true;
    });
    $scope.addOutcome = function(newOutcome) {
      newOutcome.projectId = $scope.project.id;
      OutcomeService.save(newOutcome, function(outcome) {
        $scope.project.outcomes.push(outcome);
      })

    };
  };
  return dependencies.concat(ProjectsController);
});
