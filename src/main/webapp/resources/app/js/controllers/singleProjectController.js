'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$q', '$state', '$stateParams', '$window', '$modal',
    'ProjectResource',
    'TrialverseResource',
    'TrialverseStudyResource',
    'SemanticOutcomeResource',
    'OutcomeResource',
    'SemanticInterventionResource',
    'InterventionResource',
    'CovariateOptionsResource',
    'CovariateResource',
    'AnalysisResource',
    'ANALYSIS_TYPES',
    '$modal'
  ];
  var SingleProjectController = function($scope, $q, $state, $stateParams, $window, $modal, ProjectResource, TrialverseResource,
    TrialverseStudyResource, SemanticOutcomeResource, OutcomeResource, SemanticInterventionResource, InterventionResource,
    CovariateOptionsResource, CovariateResource, AnalysisResource, ANALYSIS_TYPES) {

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
    $scope.userId = $stateParams.userUid;

    $scope.project = ProjectResource.get($stateParams);
    $scope.project.$promise.then(function() {

      $scope.loading.loaded = true;

      if ($window.config.user.id === $scope.project.owner.id) {
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

      loadCovariates();

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

    function loadCovariates() {
      // we need to get the options in order to display the definition label, as only the definition key is stored on the covariate
      $q.all([CovariateOptionsResource.query().$promise,
        CovariateResource.query({
          projectId: $scope.project.id
        }).$promise
      ]).then(function(result) {
        var optionsMap = _.keyBy(result[0], 'key');
        $scope.covariates = result[1].map(function(covariate) {
          covariate.definitionLabel = optionsMap[covariate.definitionKey].label;
          return covariate;
        });
      });
    }

    $scope.openCreateOutcomeDialog = function() {
      $modal.open({
        templateUrl: './app/js/outcome/addOutcome.html',
        scope: $scope,
        controller: 'AddOutcomeController',
        resolve: {
          callback: function() {
            return function(newOutcome) {
              $scope.outcomes.push(newOutcome);
            };
          }
        }
      });
    };

    $scope.openCreateInterventionDialog = function() {
      $modal.open({
        templateUrl: './app/js/intervention/addIntervention.html',
        scope: $scope,
        controller: 'AddInterventionController',
        resolve: {
          callback: function() {
            return function(newIntervention) {
              $scope.outcomes.push(newIntervention);
            };
          }
        }
      });
    };

    $scope.addCovariate = function() {
      $modal.open({
        templateUrl: './app/js/covariates/addCovariate.html',
        scope: $scope,
        controller: 'AddCovariateController',
        resolve: {
          outcomes: function() {
            return $scope.outcomes;
          },
          callback: function() {
            return loadCovariates;
          }
        }
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
        userUid: $scope.userId,
        projectId: $scope.project.id,
        analysisId: analysisId
      });
    };

    function findDuplicateName(list, name) {
      return _.find(list, function(item) {
        return item.name === name;
      });
    }

    $scope.checkForDuplicateInterventionName = function(name) {
      $scope.duplicateInterventionName.isDuplicate = findDuplicateName($scope.interventions, name);
    };

  };
  return dependencies.concat(SingleProjectController);
});
