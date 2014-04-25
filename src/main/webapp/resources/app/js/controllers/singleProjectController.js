'use strict';
define([], function () {
  var dependencies = ['$scope', '$state', '$stateParams', '$window',
    'ProjectResource',
    'TrialverseResource',
    'TrialverseStudyResource',
    'SemanticOutcomeResource',
    'OutcomeResource',
    'SemanticInterventionResource',
    'InterventionResource',
    'AnalysisResource',
    '$timeout'
  ];
  var ProjectsController = function ($scope, $state, $stateParams, $window, ProjectResource, TrialverseResource, TrialverseStudyResource, SemanticOutcomeResource, OutcomeResource, SemanticInterventionResource, InterventionResource, AnalysisResource, $timeout) {
    $scope.loading = {
      loaded: false
    };
    $scope.project = ProjectResource.get($stateParams);
    $scope.editMode = {
      allowEditing: false
    };

    $scope.analysisTypes = [
      {
        label: 'Single-study Benefit-Risk'
      }
    ];

    $scope.project.$promise.then(function () {
      $scope.trialverse = TrialverseResource.get({
        id: $scope.project.trialverseId
      });
      $scope.semanticOutcomes = SemanticOutcomeResource.query({
        id: $scope.project.trialverseId
      });
      $scope.semanticInterventions = SemanticInterventionResource.query({
        id: $scope.project.trialverseId
      });
      $scope.outcomes = OutcomeResource.query({
        projectId: $scope.project.id
      });
      $scope.interventions = InterventionResource.query({
        projectId: $scope.project.id
      });

      $scope.loading.loaded = true;

      $scope.editMode.allowEditing = $window.config.user.id === $scope.project.owner.id;

      $scope.studies = TrialverseStudyResource.query({
        id: $scope.project.trialverseId
      });

      $scope.studies.$promise.then(function () {
        $scope.analyses = AnalysisResource.query({
          projectId: $scope.project.id
        });
      });

    });

    $scope.goToAnalysis = function (analysisId) {
       $state.go('analysis.default', {
          'analysisId': analysisId
        });
    }

    $scope.addOutcome = function (newOutcome) {
      newOutcome.projectId = $scope.project.id;
      $scope.createOutcomeModal.close();
      this.model = {};
      OutcomeResource.save(newOutcome, function (outcome) {
        $scope.outcomes.push(outcome);
      });
    };

    $scope.addIntervention = function (newIntervention) {
      newIntervention.projectId = $scope.project.id;
      $scope.createInterventionModal.close();
      this.model = {};
      InterventionResource.save(newIntervention, function (intervention) {
        $scope.interventions.push(intervention);
      });
    };

    $scope.addAnalysis = function (newAnalysis) {
      newAnalysis.projectId = $scope.project.id;
      var savedAnalysis = AnalysisResource.save(newAnalysis);
      savedAnalysis.$promise.then(function () {
        $state.go('analysis.default', {
          projectId: savedAnalysis.projectId,
          analysisId: savedAnalysis.id
        });
      });
    };
  };
  return dependencies.concat(ProjectsController);
});
