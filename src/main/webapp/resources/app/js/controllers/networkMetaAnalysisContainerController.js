'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$scope',
    '$timeout',
    '$q',
    '$state',
    '$stateParams',
    '$modal',
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
    'CacheService',
    'currentAnalysis',
    'currentProject'
  ];

  var NetworkMetaAnalysisContainerController = function(
    $scope,
    $timeout,
    $q,
    $state,
    $stateParams,
    $modal,
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
    CacheService,
    currentAnalysis,
    currentProject
  ) {
    // functions
    $scope.selectAllInterventions = selectAllInterventions;
    $scope.deselectAllInterventions = deselectAllInterventions;
    $scope.changeCovariateInclusion = changeCovariateInclusion;
    $scope.isOverlappingIntervention = isOverlappingIntervention;
    $scope.changeSelectedOutcome = changeSelectedOutcome;
    $scope.changeInterventionInclusion = changeInterventionInclusion;
    $scope.reloadModel = reloadModel;
    $scope.gotoCreateModel = gotoCreateModel;
    $scope.editAnalysisTitle = editAnalysisTitle;

    // init
    $scope.errors = {
      isNetworkDisconnected: true,
      tableHasAmbiguousArm: false,
      hasLessThanTwoInterventions: false,
      containsMissingValue: false
    };
    $scope.isAnalysisLocked = true;
    $scope.analysis = currentAnalysis;
    $scope.project = currentProject;
    $scope.networkGraph = {};
    $scope.absoluteEvidenceTableRows = {};
    $scope.contrastEvidenceTableRows = {};
    $scope.treatmentOverlapMap = {};
    $scope.isModelCreationBlocked = setErrorsTexts();
    $scope.editMode = {
      hasModel: true,
      isUserOwner: false,
      disableEditing: true
    };

    PageTitleService.setPageTitle('NetworkMetaAnalysisContainerController', currentAnalysis.title);

    // make available for create model permission check in models.html (which is in gemtc subproject)
    $scope.userId = Number($stateParams.userUid);
    UserService.getLoginUser().then(function(result) {
      $scope.loginUserId = result ? result.id : undefined;
    });

    UserService.isLoginUserId($scope.userId).then(function(isUserOwner) {
      $scope.editMode.isUserOwner = isUserOwner;
      $scope.editMode.disableEditing = !isUserOwner || $scope.project.archived || $scope.analysis.archived;
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
        $scope.editMode.hasModel = $scope.models.length > 0;
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
            reloadModel();
          });
        } else {
          reloadModel();
        }
      });

    $scope.$on('armExclusionChanged', armExclusionChanged);
    $scope.$on('saveAnalysisAndReload', saveAnalysisAndReload);

    function editAnalysisTitle() {
      $modal.open({
        templateUrl: 'gemtc-web/app/js/analyses/editAnalysisTitle.html',
        scope: $scope,
        controller: 'EditAnalysisTitleController',
        resolve: {
          analysisTitle: function() {
            return $scope.analysis.title;
          },
          callback: function() {
            return function(newTitle) {
              AnalysisResource.setTitle($stateParams, newTitle, function() {
                CacheService.evict('analysesPromises', $scope.project.id);
                CacheService.evict('analysisPromises', $scope.analysis.id);
                $scope.analysis.title = newTitle;
              });
            };
          }
        }
      });
    }

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

    function hasIncludedStudies(rows) {
      return _.some(rows, function(row) {
        return row.numberOfMatchedInterventions > 1;
      });
    }

    function reloadModel() {
      if (!$scope.analysis.outcome) {
        // can not get data without outcome
        $scope.analysisPromise = $q.resolve();
        return;
      }
      $scope.analysis.outcome = resolveOutcomeId($scope.analysis.outcome.id);
      EvidenceTableResource
        .query({
          projectId: $scope.project.id,
          analysisId: $scope.analysis.id
        })
        .$promise.then(function(studies) {
          var includedInterventions = NetworkMetaAnalysisService.getIncludedInterventions($scope.interventions);
          var tableRows = NetworkMetaAnalysisService.transformStudiesToTableRows(
            studies, includedInterventions, $scope.analysis, $scope.covariates, $scope.treatmentOverlapMap);
          var allRows = tableRows.absolute.concat(tableRows.contrast);

          $scope.studies = studies;
          $scope.momentSelectionsTopLevel = NetworkMetaAnalysisService.buildMomentSelections(studies, $scope.analysis);
          $scope.treatmentOverlapMap = NetworkMetaAnalysisService.buildOverlappingTreatmentMap($scope.interventions, studies);

          $scope.errors.hasLessThanTwoInterventions = includedInterventions.length < 2;
          $scope.errors.hasInterventionOverlap = NetworkMetaAnalysisService.hasInterventionOverlap($scope.treatmentOverlapMap);
          $scope.errors.tableHasAmbiguousArm = NetworkMetaAnalysisService.doesModelHaveAmbiguousArms(studies, $scope.analysis);
          $scope.errors.hasInsufficientCovariateValues = NetworkMetaAnalysisService.doesModelHaveInsufficientCovariateValues(allRows);
          $scope.errors.containsMissingValue = _.find(NetworkMetaAnalysisService.buildMissingValueByStudy(allRows, $scope.momentSelectionsTopLevel));
          $scope.errors.containsMultipleResultProperties = NetworkMetaAnalysisService.doesModelContainTooManyResultProperties(allRows, $scope.momentSelectionsTopLevel);
          $scope.errors.hasMissingCovariateValues = NetworkMetaAnalysisService.hasMissingCovariateValues(tableRows);

          $scope.absoluteEvidenceTableRows = tableRows.absolute;
          $scope.contrastEvidenceTableRows = tableRows.contrast;

          $scope.analysisPromise = updateNetwork();
          $q.all([$scope.analysisPromise, UserService.getLoginUser()]).then(function() {
            $scope.isModelCreationBlocked = setErrorsTexts();
          });
        });
    }

    function changeInterventionInclusion(intervention) {
      $scope.analysis.interventionInclusions = NetworkMetaAnalysisService.buildInterventionInclusions($scope.interventions, $scope.analysis);
      if ($scope.studies && !intervention.isIncluded) {
        $scope.analysis.excludedArms = NetworkMetaAnalysisService.cleanUpExcludedArms(intervention, $scope.analysis, $scope.studies, $scope.interventions);
      }
      saveAnalysisAndReload();
    }

    function selectAllInterventions() {
      _.forEach($scope.interventions, function(intervention) {
        intervention.isIncluded = true;
      });
      $scope.analysis.interventionInclusions = NetworkMetaAnalysisService.buildInterventionInclusions($scope.interventions, $scope.analysis);
      saveAnalysisAndReload();
    }

    function deselectAllInterventions() {
      _.forEach($scope.interventions, function(intervention) {
        intervention.isIncluded = false;
      });
      $scope.analysis.excludedArms = [];
      $scope.analysis.interventionInclusions = NetworkMetaAnalysisService.buildInterventionInclusions($scope.interventions, $scope.analysis);
      saveAnalysisAndReload();
    }

    function changeSelectedOutcome() {
      $scope.errors.tableHasAmbiguousArm = false;
      $scope.analysis.excludedArms = [];
      saveAnalysisAndReload();
    }

    function isOverlappingIntervention(intervention) {
      return $scope.treatmentOverlapMap[intervention.id];
    }

    function resolveOutcomeId(outcomeId) {
      return _.find($scope.outcomes, ['id', outcomeId]);
    }

    function updateNetwork() {
      return $timeout(function() {
        var includedInterventions = NetworkMetaAnalysisService.getIncludedInterventions($scope.interventions);
        $scope.networkGraph.network = NetworkMetaAnalysisService.transformStudiesToNetwork($scope.studies, includedInterventions, $scope.analysis, $scope.momentSelectionsTopLevel);
        $scope.errors.isNetworkDisconnected = AnalysisService.isNetworkDisconnected($scope.networkGraph.network);
      });
    }

    function setErrorsTexts() {
      $scope.errorTexts = [];
      if ($scope.errors.tableHasAmbiguousArm && $scope.interventions.length > 1 && !$scope.errors.hasLessThanTwoInterventions) {
        $scope.errorTexts.push('Arms: more than one arm selected for single intervention.');
      }
      if (!$scope.interventions || $scope.interventions.length < 2 || $scope.errors.hasLessThanTwoInterventions) {
        $scope.errorTexts.push('At least two interventions are needed to perform the analysis.');
      }
      if ($scope.analysis.outcome && $scope.errors.isNetworkDisconnected) {
        $scope.errorTexts.push('Network not connected.');
      }
      if ($scope.errors.hasInterventionOverlap) {
        $scope.errorTexts.push('Overlapping interventions detected: please exclude interventions to fix this.');
      }
      if ($scope.errors.containsMissingValue) {
        $scope.errorTexts.push('The evidence table contains missing values.');
      }
      if ($scope.errors.containsMultipleResultProperties) {
        $scope.errorTexts.push('The evidence table contains studies with conflicting result properties.');
      }
      if ($scope.errors.hasMissingCovariateValues) {
        $scope.errorTexts.push('At least one included study has missing values for the overall population for selected covariates.');
      }
      return blockModelCreation();
    }

    function blockModelCreation() {
      return $scope.errorTexts.length > 0 ||
        ($scope.editMode && $scope.editMode.disableEditing) ||
        $scope.errors.hasInsufficientCovariateValues ||
        (!hasIncludedStudies($scope.absoluteEvidenceTableRows) && !hasIncludedStudies($scope.contrastEvidenceTableRows));
    }

    function armExclusionChanged() {
      $scope.errors.tableHasAmbiguousArm = false;
      updateNetwork();
      saveAnalysisAndReload();
    }

    function changeCovariateInclusion(covariate) {
      $scope.analysis.includedCovariates = NetworkMetaAnalysisService.changeCovariateInclusion(covariate, $scope.analysis);
      var saveCommand = analysisToSaveCommand($scope.analysis);
      AnalysisResource.save(saveCommand, function() {
        $scope.covariates = NetworkMetaAnalysisService.addInclusionsToCovariates($scope.covariates, $scope.analysis.includedCovariates);
        reloadModel();
      });
    }

    function saveAnalysisAndReload() {
      var saveCommand = analysisToSaveCommand($scope.analysis);
      AnalysisResource.save(saveCommand, function() {
        reloadModel();
      });
    }
  };

  return dependencies.concat(NetworkMetaAnalysisContainerController);
});
