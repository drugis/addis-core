'use strict';
define([], function() {
  var dependencies = ['$scope', '$state', '$stateParams', '$window',
    'ProjectResource',
    'TrialverseResource',
    'TrialverseStudyResource',
    'SemanticOutcomeResource',
    'OutcomeResource',
    'SemanticInterventionResource',
    'InterventionResource',
    'AnalysisResource',
    'ANALYSIS_TYPES'
  ];
  var ProjectsController = function($scope, $state, $stateParams, $window, ProjectResource, TrialverseResource, TrialverseStudyResource, SemanticOutcomeResource,
    OutcomeResource, SemanticInterventionResource, InterventionResource, AnalysisResource, ANALYSIS_TYPES) {
    $scope.loading = {
      loaded: false
    };
    $scope.editMode = {
      allowEditing: false
    };
    $scope.analysisTypes = ANALYSIS_TYPES;

    $scope.project = ProjectResource.get($stateParams);
    $scope.project.$promise.then(function() {
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

      $scope.studies.$promise.then(function() {
        $scope.analyses = AnalysisResource.query({
          projectId: $scope.project.id
        });
      });

      $scope.addOutcome = function(newOutcome) {
        newOutcome.projectId = $scope.project.id;
        $scope.createOutcomeModal.close();
        this.model = {};
        OutcomeResource
          .save(newOutcome)
          .$promise.then(function(outcome) {
            $scope.outcomes.push(outcome);
          });
      };

      $scope.addAnalysis = function(newAnalysis) {
        newAnalysis.projectId = $scope.project.id;
        AnalysisResource
          .save(newAnalysis)
          .$promise.then(function(savedAnalysis) {
            $state.go('analysis.singleStudyBenefitRisk', {
              projectId: savedAnalysis.projectId,
              analysisId: savedAnalysis.id
            });
          });
      };

      $scope.addIntervention = function(newIntervention) {
        newIntervention.projectId = $scope.project.id;
        $scope.createInterventionModal.close();
        this.model = {};
        InterventionResource
          .save(newIntervention)
          .$promise.then(function(intervention) {
            $scope.interventions.push(intervention);
          });
      };
    });

    $scope.goToAnalysis = function(analysisId, analysisTypeLabel) {
      var analysisType = _.find(ANALYSIS_TYPES, function(type) {
        return type.label === analysisTypeLabel;
      });
      $state.go(analysisType.stateName, {
        'analysisId': analysisId
      });
    };
  };
  return dependencies.concat(ProjectsController);
});