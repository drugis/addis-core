'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', '$window', 'ProjectsService', 'TrialverseService', 'SemanticOutcomeService', 'OutcomeService', 'SemanticInterventionService', 'InterventionService'];
  var ProjectsController = function($scope, $stateParams, $window, ProjectsService, TrialverseService, SemanticOutcomeService, OutcomeService, SemanticInterventionService, InterventionService) {

    $scope.loading = {loaded : false};
    $scope.project = ProjectsService.get($stateParams);
    $scope.editMode =  {allowEditing: false};

    $scope.project.$promise.then(function() {
      $scope.trialverse = TrialverseService.get({id: $scope.project.trialverseId});
      $scope.semanticOutcomes = SemanticOutcomeService.query({id: $scope.project.trialverseId});
      $scope.semanticInterventions = SemanticInterventionService.query({id: $scope.project.trialverseId});
      $scope.outcomes = OutcomeService.query({projectId: $scope.project.id});
      $scope.interventions = InterventionService.query({projectId: $scope.project.id});
      $scope.loading.loaded = true;
      $scope.editMode.allowEditing = $window.config.user.id === $scope.project.owner.id;
    });

    $scope.addOutcome = function(newOutcome) {
      newOutcome.projectId = $scope.project.id;
      $scope.createOutcomeModal.close();
      this.model = {};
      OutcomeService.save(newOutcome, function(outcome) {
        $scope.outcomes.push(outcome);
      });
    };

    $scope.addIntervention = function(newIntervention) {
      newIntervention.projectId = $scope.project.id;
      $scope.createInterventionModal.close();
      this.model = {};
      InterventionService.save(newIntervention, function(intervention) {
        $scope.interventions.push(intervention);
      });
    };
  };
  return dependencies.concat(ProjectsController);
});
