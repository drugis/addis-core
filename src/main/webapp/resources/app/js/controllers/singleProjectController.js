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
    'ANALYSIS_TYPES',
    '$modal'
  ];
  var ProjectsController = function($scope, $state, $stateParams, $window, ProjectResource, TrialverseResource,
    TrialverseStudyResource, SemanticOutcomeResource, OutcomeResource, SemanticInterventionResource, InterventionResource,
    AnalysisResource, ANALYSIS_TYPES, $modal) {

    $scope.analysesLoaded = false;
    $scope.covariatesLoaded = true;
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

      $scope.loading.loaded = true;

      if($window.config.user.id === $scope.project.owner.id) {
        $scope.editMode.allowEditing = true;
      }

      $scope.trialverse = TrialverseResource.get({
        namespaceUid: $scope.project.namespaceUid,
        version: $scope.project.datasetVersion
      });

      $scope.semanticOutcomes = SemanticOutcomeResource.query({
        namespaceUid: $scope.project.namespaceUid,
        version: $scope.project.datasetVersion
      });

      $scope.semanticInterventions = SemanticInterventionResource.query({
        namespaceUid: $scope.project.namespaceUid,
        version: $scope.project.datasetVersion
      });

      $scope.outcomes = OutcomeResource.query({
        projectId: $scope.project.id
      });

      $scope.interventions = InterventionResource.query({
        projectId: $scope.project.id
      });

      $scope.studies = TrialverseStudyResource.query({
        namespaceUid: $scope.project.namespaceUid,
        version: $scope.project.datasetVersion
      });

      $scope.studies.$promise.then(function() {
        $scope.analyses = AnalysisResource.query({
          projectId: $scope.project.id
        }, function() {
          $scope.analysesLoaded = true;
        });
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

    $scope.addCovariate = function() {
      $modal.open({
        templateUrl: './app/js/covariates/addCovariate.html',
        scope: $scope,
        controller: 'AddCovariateController'
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

    $scope.goToAnalysis = function(analysisId, analysisTypeLabel) {
      var analysisType = _.find(ANALYSIS_TYPES, function(type) {
        return type.label === analysisTypeLabel;
      });
      //todo if analysis is gemtc type and has a problem go to models view
      $state.go(analysisType.stateName, {
        projectId: $scope.project.id,
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