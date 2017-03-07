'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$q', '$state', '$stateParams', 'currentAnalysis', 'currentProject', 'OutcomeResource',
    'InterventionResource', 'CovariateResource', 'ModelResource', 'NetworkMetaAnalysisService', 'AnalysisService',
    'EvidenceTableResource', 'UserService'
  ];

  var NetworkMetaAnalysisContainerController = function($scope, $q, $state, $stateParams, currentAnalysis, currentProject,
    OutcomeResource, InterventionResource, CovariateResource, ModelResource, NetworkMetaAnalysisService, AnalysisService,
    EvidenceTableResource, UserService) {

    $scope.isAnalysisLocked = true;
    $scope.isNetworkDisconnected = true;
    $scope.hasModel = true;
    $scope.tableHasAmbiguousArm = false;
    $scope.hasLessThanTwoInterventions = false;
    $scope.containsMissingValue = false;
    $scope.analysis = currentAnalysis;
    $scope.project = currentProject;
    $scope.networkGraph = {};
    $scope.trialData = {};
    $scope.treatmentOverlapMap = {};
    $scope.loading = {
      loaded: false
    };


    var isUserOwner = false;

    // make available for create model permission check in models.html (which is in gemtc subproject)
    $scope.userId = Number($stateParams.userUid);

    if (UserService.hasLoggedInUser()) {
      $scope.loginUserId = (UserService.getLoginUser()).id;
      isUserOwner = UserService.isLoginUserId($scope.project.owner.id);
    }

    $scope.editMode = {
      isUserOwner: isUserOwner,
      disableEditing: !isUserOwner || $scope.project.archived || $scope.analysis.archived
    };

    $scope.models = ModelResource.query({
      projectId: $stateParams.projectId,
      analysisId: $stateParams.analysisId
    });

    var outcomesPromise = OutcomeResource.query({
      projectId: $stateParams.projectId
    }).$promise;

    outcomesPromise.then(function(outcomes) {
      $scope.outcomes = _.map(outcomes, function(outcome) {
        if (outcome.direction === 1) {
          outcome.name = outcome.name + ' (higher is better)';
        } else {
          outcome.name = outcome.name + ' (lower is better)';
        }
        return outcome;
      });
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
        outcomesPromise,
        $scope.interventions.$promise,
        $scope.covariates.$promise,
      ])
      .then(function() {
        $scope.hasModel = $scope.models.length > 0;
        $scope.interventions = _.orderBy($scope.interventions, function(intervention) {
          return intervention.name.toLowerCase();
        });
        $scope.interventions = NetworkMetaAnalysisService.addInclusionsToInterventions($scope.interventions, $scope.analysis.interventionInclusions);
        $scope.covariates = NetworkMetaAnalysisService.addInclusionsToCovariates($scope.covariates, $scope.analysis.includedCovariates);
        $scope.project.datasetVersionUuid = _.split($scope.project.datasetVersion, '/versions/')[1];
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

    $scope.doesInterventionHaveAmbiguousArms = function(drugId, studyUuid) {
      return NetworkMetaAnalysisService.doesInterventionHaveAmbiguousArms(drugId, studyUuid, $scope.trialverseData, $scope.analysis);
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
          $scope.momentSelections = NetworkMetaAnalysisService.buildMomentSelections(trialverseData, $scope.analysis);
          var includedInterventions = NetworkMetaAnalysisService.getIncludedInterventions($scope.interventions);
          updateNetwork();
          $scope.treatmentOverlapMap = NetworkMetaAnalysisService.buildOverlappingTreatmentMap($scope.interventions, trialverseData);
          $scope.trialData = NetworkMetaAnalysisService.transformTrialDataToTableRows(trialverseData, includedInterventions, $scope.analysis, $scope.covariates, $scope.treatmentOverlapMap);
          $scope.tableHasAmbiguousArm = NetworkMetaAnalysisService.doesModelHaveAmbiguousArms(trialverseData, $scope.analysis);
          $scope.hasInsufficientCovariateValues = NetworkMetaAnalysisService.doesModelHaveInsufficientCovariateValues($scope.trialData);
          $scope.hasLessThanTwoInterventions = includedInterventions.length < 2;
          $scope.hasTreatmentOverlap = hasTreatmentOverlap();
          $scope.isMissingByStudyMap = NetworkMetaAnalysisService.buildMissingValueByStudyMap(trialverseData, $scope.analysis, $scope.momentSelections);
          $scope.containsMissingValue = _.find($scope.isMissingByStudyMap);
          $scope.isModelCreationBlocked = checkCanNotCreateModel();
          $scope.showStdErr = NetworkMetaAnalysisService.checkStdErrShow($scope.trialData);
          $scope.showSigmaN = NetworkMetaAnalysisService.checkSigmaNShow($scope.trialData);
          $scope.measurementType = NetworkMetaAnalysisService.getMeasurementType($scope.trialverseData);
          $scope.loading.loaded = true;
        });
    };

    $scope.changeInterventionInclusion = function(intervention) {
      $scope.analysis.interventionInclusions = NetworkMetaAnalysisService.buildInterventionInclusions($scope.interventions, $scope.analysis);
      if ($scope.trialverseData && !intervention.isIncluded) {
        $scope.analysis.excludedArms = NetworkMetaAnalysisService.cleanUpExcludedArms(intervention, $scope.analysis, $scope.trialverseData, $scope.interventions);
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

    function updateNetwork() {
      var includedInterventions = NetworkMetaAnalysisService.getIncludedInterventions($scope.interventions);
      $scope.networkGraph.network = NetworkMetaAnalysisService.transformTrialDataToNetwork($scope.trialverseData, includedInterventions, $scope.analysis, $scope.momentSelections);
      $scope.isNetworkDisconnected = AnalysisService.isNetworkDisconnected($scope.networkGraph.network);
    }


    function checkCanNotCreateModel() {
      return ($scope.editMode && $scope.editMode.disableEditing) ||
        $scope.tableHasAmbiguousArm ||
        $scope.interventions.length < 2 ||
        $scope.isNetworkDisconnected ||
        $scope.hasLessThanTwoInterventions ||
        $scope.hasTreatmentOverlap ||
        $scope.containsMissingValue ||
        $scope.hasInsufficientCovariateValues ||
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

    $scope.changeMeasurementMoment = function(newMeasurementMoment, dataRow) {
      // always remove old inclusion for this study
      $scope.analysis.includedMeasurementMoments = _.reject($scope.analysis.includedMeasurementMoments, ['study', dataRow.studyUri]);

      if (!newMeasurementMoment.isDefault) {
        var newInclusion = {
          analysisId: $scope.analysis.id,
          study: dataRow.studyUri,
          measurementMoment: newMeasurementMoment.uri
        };
        $scope.analysis.includedMeasurementMoments.push(newInclusion);
      }

      $scope.analysis.$save(function() {
        $scope.reloadModel();
      });
    };

  };

  return dependencies.concat(NetworkMetaAnalysisContainerController);
});
