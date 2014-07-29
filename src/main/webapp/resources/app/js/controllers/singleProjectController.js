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
    $scope.duplicateOutcomeName = {
      isDuplicate: false
    };
    $scope.duplicateInterventionName = {
      isDuplicate: false
    };
    $scope.analysisTypes = ANALYSIS_TYPES;

    $scope.project = ProjectResource.get($stateParams);
    $scope.project.$promise.then(function() {
      $scope.trialverse = TrialverseResource.get({
        id: $scope.project.namespaceUid
      });
      $scope.semanticOutcomes = SemanticOutcomeResource.query({
        id: $scope.project.namespaceUid
      });
      $scope.semanticInterventions = SemanticInterventionResource.query({
        id: $scope.project.namespaceUid
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
        id: $scope.project.namespaceUid
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

      $scope.addAnalysis = function(newAnalysis) {
        newAnalysis.projectId = $scope.project.id;
        AnalysisResource
          .save(newAnalysis)
          .$promise.then(function(savedAnalysis) {
            $scope.goToAnalysis(savedAnalysis.id, savedAnalysis.analysisType);
          });
      };
    });

    $scope.goToAnalysis = function(analysisId, analysisTypeLabel) {
      var analysisType = _.find(ANALYSIS_TYPES, function(type) {
        return type.label === analysisTypeLabel;
      });
      $state.go(analysisType.stateName, {
        analysisId: analysisId
      });
    };

    function findDuplicateName(list, name) {
      return _.find(list, function(item) {
        return item.name === name;
      });
    }

    $scope.checkForDuplicateOutcomeName = function(name) {
      $scope.duplicateOutcomeName.isDuplicate = findDuplicateName($scope.outcomes, name);
    };

    $scope.checkForDuplicateInterventionName = function(name) {
      $scope.duplicateInterventionName.isDuplicate = findDuplicateName($scope.interventions, name);
    };

  };
  return dependencies.concat(ProjectsController);
});