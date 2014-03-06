'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', '$window', 'ProjectsService', 'TrialverseService', 'SemanticOutcomeService', 'OutcomeService'];
  var ProjectsController = function($scope, $stateParams, $window, ProjectsService, TrialverseService, SemanticOutcomeService, OutcomeService) {

    $scope.loading = {loaded : false};
    $scope.project = ProjectsService.get($stateParams);
    $scope.editMode =  {allowEditing: false};

    $scope.project.$promise.then(function() {
      $scope.trialverse = TrialverseService.get({id: $scope.project.trialverseId});
      $scope.semanticOutcomes = SemanticOutcomeService.query({id: $scope.project.trialverseId});
      $scope.loading.loaded = true;
      $scope.editMode.allowEditing = $window.config.user.id === $scope.project.owner.id;
    });

    $scope.addOutcome = function(newOutcome) {
      newOutcome.projectId = $scope.project.id;
      $scope.createOutcomeModal.close();
      OutcomeService.save(newOutcome, function(outcome) {
        $scope.project.outcomes.push(outcome);
      });
    };
  };
  return dependencies.concat(ProjectsController);
});
