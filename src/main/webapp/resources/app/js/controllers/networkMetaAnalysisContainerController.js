'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$q', '$state', '$window', '$stateParams', 'currentAnalysis', 'currentProject', 'OutcomeResource',
    'InterventionResource', 'CovariateResource', 'ModelResource', 'NetworkMetaAnalysisService', 'EvidenceTableResource'
  ];

  var NetworkMetaAnalysisContainerController = function($scope, $q, $state, $window, $stateParams, currentAnalysis, currentProject,
    OutcomeResource, InterventionResource, CovariateResource, ModelResource, NetworkMetaAnalysisService, EvidenceTableResource) {

    $scope.isAnalysisLocked = true;
    $scope.isNetworkDisconnected = true;
    $scope.hasModel = true;
    $scope.tableHasAmbiguousArm = false;
    $scope.hasLessThanTwoInterventions = false;
    $scope.analysis = currentAnalysis;
    $scope.project = currentProject;
    $scope.networkGraph = {};
    $scope.trialData = {};
    $scope.treatmentOverlapMap = {};
    $scope.loading = {
      loaded: false
    };
    // make available for create model permission check in models.html (which is in gemtc subproject)
    $scope.userId = Number($stateParams.userUid);
    $scope.loginUserId = $window.config.user.id;

    $scope.models = ModelResource.query({
      projectId: $stateParams.projectId,
      analysisId: $stateParams.analysisId
    });

    $scope.outcomes = OutcomeResource.query({
      projectId: $stateParams.projectId
    });

    $scope.interventions = InterventionResource.query({
      projectId: $stateParams.projectId
    });

    $scope.covariates = CovariateResource.query({
      projectId: $stateParams.projectId
    });

    $q.all([
      $scope.analysis.$promise,
      $scope.project.$promise,
      $scope.models.$promise,
      $scope.outcomes.$promise,
      $scope.interventions.$promise,
      $scope.covariates.$promise
    ])
      .then(function() {
        $scope.hasModel = $scope.models.length > 0;
        $scope.interventions = NetworkMetaAnalysisService.addInclusionsToInterventions($scope.interventions, $scope.analysis.interventionInclusions);
        $scope.covariates = NetworkMetaAnalysisService.addInclusionsToCovariates($scope.covariates, $scope.analysis.includedCovariates);
        if (!$scope.analysis.outcome && $scope.outcomes.length > 0) {
          // set first outcome as default outcome
          $scope.analysis.outcome = $scope.outcomes[0];
          $scope.analysis.$save(function() {
            $scope.reloadModel();
          });
        } else {
          $scope.reloadModel();
        }
      });

    $scope.gotoCreateModel = function() {
      $state.go('createModel', {
        userUid: $stateParams.userUid,
        projectId: $stateParams.projectId,
        analysisId: $stateParams.analysisId
      });
    };

    $scope.lessThanTwoInterventionArms = function(dataRow) {
      return dataRow.numberOfMatchedInterventions < 2;
    };

    $scope.hasIncludedStudies = function() {
      return _.find($scope.trialData, function(dataRow) {
        return !$scope.lessThanTwoInterventionArms(dataRow);
      });
    };

    $scope.editMode = {
      isUserOwner: $window.config.user.id === $scope.project.owner.id
    };
    $scope.editMode.disableEditing = !$scope.editMode.isUserOwner;

    $scope.doesInterventionHaveAmbiguousArms = function(drugId, studyUid) {
      return NetworkMetaAnalysisService.doesInterventionHaveAmbiguousArms(drugId, studyUid, $scope.trialverseData, $scope.analysis);
    };

    $scope.reloadModel = function reloadModel() {
      if (!$scope.analysis.outcome) {
        // can not get data without outcome
        $scope.loading.loaded = true;
        return;
      }
      $scope.analysis.outcome = resolveOutcomeId($scope.analysis.outcome.id);
      EvidenceTableResource
        .query({
          projectId: $scope.project.id,
          analysisId: $scope.analysis.id
        })
        .$promise
        .then(function(trialverseData) {
          $scope.trialverseData = trialverseData;
          updateNetwork();
          var includedInterventions = NetworkMetaAnalysisService.getIncludedInterventions($scope.interventions);
          $scope.treatmentOverlapMap = NetworkMetaAnalysisService.buildOverlappingTreatmentMap($scope.interventions, trialverseData);
          $scope.trialData = NetworkMetaAnalysisService.transformTrialDataToTableRows(trialverseData, includedInterventions, $scope.analysis, $scope.covariates, $scope.treatmentOverlapMap);
          $scope.tableHasAmbiguousArm = NetworkMetaAnalysisService.doesModelHaveAmbiguousArms(trialverseData, $scope.analysis);
          $scope.hasLessThanTwoInterventions = includedInterventions.length < 2;
          $scope.hasTreatmentOverlap = hasTreatmentOverlap();
          $scope.isModelCreationBlocked = checkCanNotCreateModel();
          $scope.loading.loaded = true;
        });
    };

    $scope.changeInterventionInclusion = function(intervention) {
      $scope.analysis.interventionInclusions = NetworkMetaAnalysisService.buildInterventionInclusions($scope.interventions, $scope.analysis);
      if ($scope.trialverseData && !intervention.isIncluded) {
        $scope.analysis.excludedArms = NetworkMetaAnalysisService.cleanUpExcludedArms(intervention, $scope.analysis, $scope.trialverseData);
      }
      $scope.analysis.$save(function() {
        $scope.reloadModel();
      });
    };

    $scope.changeSelectedOutcome = function() {
      $scope.tableHasAmbiguousArm = false;
      $scope.analysis.excludedArms = [];
      $scope.analysis.$save(function() {
        $scope.reloadModel();
      });
    };

    $scope.isOverlappingIntervention = function(intervention) {
      return $scope.treatmentOverlapMap[intervention.id];
    };

    var hasTreatmentOverlap = function() {
      var overlapCount = _.reduce($scope.treatmentOverlapMap, function(count) {
        return ++count;
      }, 0);
      return overlapCount > 0;
    };

    function resolveOutcomeId(outcomeId) {
      return _.find($scope.outcomes, function matchOutcome(outcome) {
        return outcomeId === outcome.id;
      });
    }

    function addIncludedInterventionUri(memo, intervention) {
      if (intervention.isIncluded) {
        memo.push(intervention.semanticInterventionUri);
      }
      return memo;
    }

    function addIncludedCovariateDefinitionKey(accum, covariate) {
      if (covariate.isIncluded) {
        accum.push(covariate.definitionKey);
      }
      return accum;
    }

    function updateNetwork() {
      var includedInterventions = NetworkMetaAnalysisService.getIncludedInterventions($scope.interventions);
      $scope.networkGraph.network = NetworkMetaAnalysisService.transformTrialDataToNetwork($scope.trialverseData, includedInterventions, $scope.analysis);
      $scope.isNetworkDisconnected = NetworkMetaAnalysisService.isNetworkDisconnected($scope.networkGraph.network);
    }


    function checkCanNotCreateModel() {
      return ($scope.editMode && $scope.editMode.disableEditing) ||
        $scope.tableHasAmbiguousArm ||
        $scope.interventions.length < 2 ||
        $scope.isNetworkDisconnected ||
        $scope.hasLessThanTwoInterventions ||
        $scope.hasTreatmentOverlap ||
        !$scope.hasIncludedStudies();
    }
    $scope.isModelCreationBlocked = checkCanNotCreateModel();

    $scope.changeArmExclusion = function(dataRow) {
      $scope.tableHasAmbiguousArm = false;
      $scope.analysis = NetworkMetaAnalysisService.changeArmExclusion(dataRow, $scope.analysis);
      updateNetwork();
      $scope.analysis.$save(function() {
        $scope.reloadModel();
      });
    };

    $scope.changeCovariateInclusion = function(covariate) {
      $scope.analysis.includedCovariates = NetworkMetaAnalysisService.changeCovariateInclusion(covariate, $scope.analysis);
      $scope.analysis.$save(function() {
        $scope.covariates = NetworkMetaAnalysisService.addInclusionsToCovariates($scope.covariates, $scope.analysis.includedCovariates);
        $scope.reloadModel();
      });
    };
  };

  return dependencies.concat(NetworkMetaAnalysisContainerController);
});
