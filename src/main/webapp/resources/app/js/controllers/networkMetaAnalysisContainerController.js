'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = [
    '$scope',
    '$timeout',
    '$q',
    '$state',
    '$stateParams',
    'AnalysisResource',
    'AnalysisService',
    'CovariateResource',
    'EvidenceTableResource',
    'InterventionResource',
    'ModelResource',
    'NetworkMetaAnalysisService',
    'OutcomeResource',
    'PageTitleService',
    'UserService',
    'currentAnalysis',
    'currentProject'
  ];

  var NetworkMetaAnalysisContainerController = function(
    $scope,
    $timeout,
    $q,
    $state,
    $stateParams,
    AnalysisResource,
    AnalysisService,
    CovariateResource,
    EvidenceTableResource,
    InterventionResource,
    ModelResource,
    NetworkMetaAnalysisService,
    OutcomeResource,
    PageTitleService,
    UserService,
    currentAnalysis,
    currentProject
  ) {
    // functions
    $scope.selectAllInterventions = selectAllInterventions;
    $scope.deselectAllInterventions = deselectAllInterventions;
    $scope.changeMeasurementMoment = changeMeasurementMoment;
    $scope.changeCovariateInclusion = changeCovariateInclusion;
    $scope.changeArmExclusion = changeArmExclusion;
    $scope.isOverlappingIntervention = isOverlappingIntervention;
    $scope.changeSelectedOutcome = changeSelectedOutcome;
    $scope.changeInterventionInclusion = changeInterventionInclusion;
    $scope.reloadModel = reloadModel;
    $scope.doesInterventionHaveAmbiguousArms = doesInterventionHaveAmbiguousArms;
    $scope.hasIncludedStudies = hasIncludedStudies;
    $scope.gotoCreateModel = gotoCreateModel;
    $scope.lessThanTwoInterventionArms = lessThanTwoInterventionArms;

    // init
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
    $scope.isModelCreationBlocked = checkCanNotCreateModel();

    PageTitleService.setPageTitle('NetworkMetaAnalysisContainerController', currentAnalysis.title);

    // make available for create model permission check in models.html (which is in gemtc subproject)
    $scope.userId = Number($stateParams.userUid);
    var isUserOwner = false;

    $scope.columns = [{
      label: 'subject with event',
      helpKey: 'count',
      dataKey: 'rate'
    }, {
      label: 'mean',
      helpKey: 'mean',
      dataKey: 'mu'
    }, {
      label: 'standard deviation',
      helpKey: 'standard-deviation',
      dataKey: 'sigma'
    }, {
      label: 'N',
      helpKey: 'sample-size',
      dataKey: 'sampleSize'
    }, {
      label: 'standard error',
      helpKey: 'standard-error',
      dataKey: 'stdErr'
    }, {
      label: 'exposure',
      helpKey: 'exposure',
      dataKey: 'exposure'
    }];

    UserService.getLoginUser().then(function(user) {
      if(user){
        $scope.loginUserId = user.id;
        isUserOwner = UserService.isLoginUserId($scope.project.owner.id);
      }
      $scope.editMode = {
        isUserOwner: isUserOwner,
        disableEditing: !isUserOwner || $scope.project.archived || $scope.analysis.archived
      };
    });

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
          var saveCommand = analysisToSaveCommand($scope.analysis);
          AnalysisResource.save(saveCommand, function() {
            $scope.reloadModel();
          });
        } else {
          $scope.reloadModel();
        }
      });

    function analysisToSaveCommand(analysis) {
      return {
        id: analysis.id,
        projectId: analysis.projectId,
        analysis: analysis
      };
    }

    function gotoCreateModel() {
      $state.go('createModel', {
        userUid: $stateParams.userUid,
        projectId: $stateParams.projectId,
        analysisId: $stateParams.analysisId
      });
    }

    function lessThanTwoInterventionArms(dataRow) {
      return dataRow.numberOfMatchedInterventions < 2;
    }

    function hasIncludedStudies() {
      return _.find($scope.trialData, function(dataRow) {
        return !$scope.lessThanTwoInterventionArms(dataRow);
      });
    }

    function doesInterventionHaveAmbiguousArms(drugId, studyUuid) {
      return NetworkMetaAnalysisService.doesInterventionHaveAmbiguousArms(drugId, studyUuid, $scope.trialverseData, $scope.analysis);
    }

    function reloadModel() {
      if (!$scope.analysis.outcome) {
        // can not get data without outcome
        return;
      }
      $scope.analysis.outcome = resolveOutcomeId($scope.analysis.outcome.id);
      EvidenceTableResource
        .query({
          projectId: $scope.project.id,
          analysisId: $scope.analysis.id
        })
        .$promise.then(function(trialverseData) {
          $scope.trialverseData = trialverseData;
          $scope.momentSelections = NetworkMetaAnalysisService.buildMomentSelections(trialverseData, $scope.analysis);
          var includedInterventions = NetworkMetaAnalysisService.getIncludedInterventions($scope.interventions);
          $scope.treatmentOverlapMap = NetworkMetaAnalysisService.buildOverlappingTreatmentMap($scope.interventions, trialverseData);
          $scope.trialData = NetworkMetaAnalysisService.transformTrialDataToTableRows(
            trialverseData, includedInterventions, $scope.analysis, $scope.covariates, $scope.treatmentOverlapMap);
          $scope.tableHasAmbiguousArm = NetworkMetaAnalysisService.doesModelHaveAmbiguousArms(trialverseData, $scope.analysis);
          $scope.hasInsufficientCovariateValues = NetworkMetaAnalysisService.doesModelHaveInsufficientCovariateValues($scope.trialData);
          $scope.hasLessThanTwoInterventions = includedInterventions.length < 2;
          $scope.hasTreatmentOverlap = hasTreatmentOverlap();
          $scope.isMissingByStudyMap = NetworkMetaAnalysisService.buildMissingValueByStudyMap(trialverseData, $scope.analysis, $scope.momentSelections);
          $scope.containsMissingValue = _.find($scope.isMissingByStudyMap);
          $scope.measurementType = NetworkMetaAnalysisService.getMeasurementType($scope.trialverseData);
          $scope.showColumn = NetworkMetaAnalysisService.checkColumnsToShow($scope.trialData, $scope.measurementType);

          $scope.analysisPromise = updateNetwork()
          $q.all([$scope.analysisPromise, UserService.getLoginUser()]).then(function() {
            $scope.isModelCreationBlocked = checkCanNotCreateModel();
          });
        });
    }

    function changeInterventionInclusion(intervention) {
      $scope.analysis.interventionInclusions = NetworkMetaAnalysisService.buildInterventionInclusions($scope.interventions, $scope.analysis);
      if ($scope.trialverseData && !intervention.isIncluded) {
        $scope.analysis.excludedArms = NetworkMetaAnalysisService.cleanUpExcludedArms(intervention, $scope.analysis, $scope.trialverseData, $scope.interventions);
      }
      var saveCommand = analysisToSaveCommand($scope.analysis);
      AnalysisResource.save(saveCommand, function() {
        $scope.reloadModel();
      });
    }

    function selectAllInterventions() {
      _.forEach($scope.interventions, function(intervention) {
        intervention.isIncluded = true;
      });
      $scope.analysis.interventionInclusions = NetworkMetaAnalysisService.buildInterventionInclusions($scope.interventions, $scope.analysis);
      var saveCommand = analysisToSaveCommand($scope.analysis);
      AnalysisResource.save(saveCommand, function() {
        $scope.reloadModel();
      });
    }

    function deselectAllInterventions() {
      _.forEach($scope.interventions, function(intervention) {
        intervention.isIncluded = false;
      });
      $scope.analysis.excludedArms = [];
      $scope.analysis.interventionInclusions = NetworkMetaAnalysisService.buildInterventionInclusions($scope.interventions, $scope.analysis);
      var saveCommand = analysisToSaveCommand($scope.analysis);
      AnalysisResource.save(saveCommand, function() {
        $scope.reloadModel();
      });
    }

    function changeSelectedOutcome() {
      $scope.tableHasAmbiguousArm = false;
      $scope.analysis.excludedArms = [];
      var saveCommand = analysisToSaveCommand($scope.analysis);
      AnalysisResource.save(saveCommand, function() {
        $scope.reloadModel();
      });
    }

    function isOverlappingIntervention(intervention) {
      return $scope.treatmentOverlapMap[intervention.id];
    }

    function hasTreatmentOverlap() {
      var overlapCount = _.reduce($scope.treatmentOverlapMap, function(count) {
        return ++count;
      }, 0);
      return overlapCount > 0;
    }

    function resolveOutcomeId(outcomeId) {
      return _.find($scope.outcomes, function matchOutcome(outcome) {
        return outcomeId === outcome.id;
      });
    }

    function updateNetwork() {
      return $timeout(function() {
        var includedInterventions = NetworkMetaAnalysisService.getIncludedInterventions($scope.interventions);
        $scope.networkGraph.network = NetworkMetaAnalysisService.transformTrialDataToNetwork($scope.trialverseData, includedInterventions, $scope.analysis, $scope.momentSelections);
        $scope.isNetworkDisconnected = AnalysisService.isNetworkDisconnected($scope.networkGraph.network);
      });
    }


    function checkCanNotCreateModel() {
      return ($scope.editMode && $scope.editMode.disableEditing) ||
        $scope.tableHasAmbiguousArm ||
        !$scope.interventions || $scope.interventions.length < 2 ||
        $scope.isNetworkDisconnected ||
        $scope.hasLessThanTwoInterventions ||
        $scope.hasTreatmentOverlap ||
        $scope.containsMissingValue ||
        $scope.hasInsufficientCovariateValues ||
        !$scope.hasIncludedStudies();
    }

    function changeArmExclusion(dataRow) {
      $scope.tableHasAmbiguousArm = false;
      $scope.analysis = NetworkMetaAnalysisService.changeArmExclusion(dataRow, $scope.analysis);
      updateNetwork();
      var saveCommand = analysisToSaveCommand($scope.analysis);
      AnalysisResource.save(saveCommand, function() {
        $scope.reloadModel();
      });
    }

    function changeCovariateInclusion(covariate) {
      $scope.analysis.includedCovariates = NetworkMetaAnalysisService.changeCovariateInclusion(covariate, $scope.analysis);
      var saveCommand = analysisToSaveCommand($scope.analysis);
      AnalysisResource.save(saveCommand, function() {
        $scope.covariates = NetworkMetaAnalysisService.addInclusionsToCovariates($scope.covariates, $scope.analysis.includedCovariates);
        $scope.reloadModel();
      });
    }

    function changeMeasurementMoment(newMeasurementMoment, dataRow) {
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

      var saveCommand = analysisToSaveCommand($scope.analysis);
      AnalysisResource.save(saveCommand, function() {
        $scope.reloadModel();
      });
    }

  };

  return dependencies.concat(NetworkMetaAnalysisContainerController);
});
