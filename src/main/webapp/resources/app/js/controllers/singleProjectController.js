'use strict';
define([], function() {
  var dependencies = ['$scope', '$state', '$stateParams', '$window',
    'ProjectsService',
    'TrialverseService',
    'SemanticOutcomeService',
    'OutcomeService',
    'SemanticInterventionService',
    'InterventionService',
    'AnalysisService'];
  var ProjectsController = function($scope, $state, $stateParams, $window, ProjectsService, TrialverseService,
      SemanticOutcomeService, OutcomeService, SemanticInterventionService, InterventionService, AnalysisService) {
    $scope.loading = {loaded : false};
    $scope.project = ProjectsService.get($stateParams);
    $scope.editMode =  {allowEditing: false};

    $scope.analysisTypes = [{label : 'Single-study Benefit-Risk'}];

    $scope.project.$promise.then(function() {
      $scope.trialverse = TrialverseService.get({id: $scope.project.trialverseId});
      $scope.semanticOutcomes = SemanticOutcomeService.query({id: $scope.project.trialverseId});
      $scope.semanticInterventions = SemanticInterventionService.query({id: $scope.project.trialverseId});
      $scope.outcomes = OutcomeService.query({projectId: $scope.project.id});
      $scope.interventions = InterventionService.query({projectId: $scope.project.id});
      $scope.analyses = AnalysisService.query({projectId: $scope.project.id});
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

    $scope.addAnalysis = function(newAnalysis) {
      newAnalysis.projectId = $scope.project.id;
      var savedAnalysis = AnalysisService.save(newAnalysis);
      savedAnalysis.$promise.then(function() {
        $state.go('analysis', {projectId: savedAnalysis.projectId, analysisId: savedAnalysis.id});
      });
    };

  };
  return dependencies.concat(ProjectsController);
});
