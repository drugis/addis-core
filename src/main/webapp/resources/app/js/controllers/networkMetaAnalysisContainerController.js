'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$q', '$state', '$window', '$stateParams', 'currentAnalysis', 'currentProject', 'OutcomeResource',
    'InterventionResource', 'CovariateResource', 'ModelResource', 'NetworkMetaAnalysisService', 'TrialverseTrialDataResource'
  ];

  var NetworkMetaAnalysisContainerController = function($scope, $q, $state, $window, $stateParams, currentAnalysis, currentProject,
    OutcomeResource, InterventionResource, CovariateResource, ModelResource, NetworkMetaAnalysisService, TrialverseTrialDataResource) {

    $scope.isAnalysisLocked = true;
    $scope.isNetworkDisconnected = true;
    $scope.hasModel = true;
    $scope.tableHasAmbiguousArm = false;
    $scope.hasLessThanTwoInterventions = false;
    $scope.analysis = currentAnalysis;
    $scope.project = currentProject;
    $scope.networkGraph = {};
    $scope.trialData = {};
    $scope.loading = {
      loaded: false
    };
    $scope.userId = $stateParams.userUid;

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
        $scope.interventions = NetworkMetaAnalysisService.addInclusionsToInterventions($scope.interventions, $scope.analysis.includedInterventions);
        $scope.covariates = NetworkMetaAnalysisService.addInclusionsToCovariates($scope.covariates, $scope.analysis.covariateInclusions);
        $scope.analysis.outcome = _.find($scope.outcomes, $scope.matchOutcome);
        $scope.reloadModel();
      });

    $scope.gotoCreateModel = function() {
      $state.go('createModel', {
        userUid: $stateParams.userUid,
        projectId: $stateParams.projectId,
        analysisId: $stateParams.analysisId
      });
    };

    $scope.lessThanTwoInterventionArms = function(dataRow) {
      var matchedAndIncludedRows = _.filter(dataRow.studyRows, function(studyRow) {
        return studyRow.intervention !== 'unmatched' && studyRow.included;
      });
      var matchedInterventions = _.uniq(_.map(matchedAndIncludedRows, 'intervention'));
      return matchedInterventions.length < 2;
    };

    $scope.editMode = {
      isUserOwner: $window.config.user.id === $scope.project.owner.id
    };
    $scope.editMode.disableEditing = !$scope.editMode.isUserOwner;

    $scope.matchOutcome = function matchOutcome(outcome) {
      return $scope.analysis.outcome && $scope.analysis.outcome.id === outcome.id;
    };

    $scope.doesInterventionHaveAmbiguousArms = function(drugId, studyUid) {
      return NetworkMetaAnalysisService.doesInterventionHaveAmbiguousArms(drugId, studyUid, $scope.trialverseData, $scope.analysis);
    };

    $scope.reloadModel = function reloadModel() {
      $scope.analysis.outcome = _.find($scope.outcomes, $scope.matchOutcome);
      if (!$scope.analysis.outcome) {
        // can not get data without outcome
        $scope.loading.loaded = true;
        return
      }
      TrialverseTrialDataResource
        .get({
          namespaceUid: $scope.project.namespaceUid,
          outcomeUri: $scope.analysis.outcome.semanticOutcomeUri,
          interventionUris: _.reduce($scope.interventions, addIncludedInterventionUri, []),
          covariateKeys: _.reduce($scope.covariates, addIncludedCovariateDefinitionKey, []),
          version: $scope.project.datasetVersion
        })
        .$promise
        .then(function(trialverseData) {
          $scope.trialverseData = trialverseData;
          updateNetwork();
          var includedInterventions = getIncludedInterventions($scope.interventions);
          $scope.trialData = NetworkMetaAnalysisService.transformTrialDataToTableRows(trialverseData, includedInterventions, $scope.analysis.excludedArms, $scope.covariates);
          $scope.tableHasAmbiguousArm = NetworkMetaAnalysisService.doesModelHaveAmbiguousArms($scope.trialverseData, $scope.interventions, $scope.analysis);
          $scope.hasLessThanTwoInterventions = includedInterventions.length < 2;
          $scope.isModelCreationBlocked = checkCanNotCreateModel();
          $scope.loading.loaded = true;
        });
    }

    $scope.changeInterventionInclusion = function(intervention) {
      $scope.analysis.includedInterventions = NetworkMetaAnalysisService.buildInterventionInclusions($scope.interventions, $scope.analysis);
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

    function getIncludedInterventions(interventions) {
      return _.filter(interventions, function(intervention) {
        return intervention.isIncluded;
      });
    }

    function updateNetwork() {
      var includedInterventions = getIncludedInterventions($scope.interventions);
      $scope.networkGraph.network = NetworkMetaAnalysisService.transformTrialDataToNetwork($scope.trialverseData, includedInterventions, $scope.analysis.excludedArms);
      $scope.isNetworkDisconnected = NetworkMetaAnalysisService.isNetworkDisconnected($scope.networkGraph.network);
    }


    function checkCanNotCreateModel() {
      return ($scope.editMode && $scope.editMode.disableEditing) ||
        $scope.tableHasAmbiguousArm ||
        $scope.interventions.length < 2 ||
        $scope.isNetworkDisconnected ||
        $scope.hasLessThanTwoInterventions;
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
      $scope.analysis.covariateInclusions = NetworkMetaAnalysisService.changeCovariateInclusion(covariate, $scope.analysis.covariateInclusions);
      $scope.analysis.$save(function() {
        $scope.covariates = NetworkMetaAnalysisService.addInclusionsToCovariates($scope.covariates, $scope.analysis.covariateInclusions);
        $scope.reloadModel();
      });
    };
  };

  return dependencies.concat(NetworkMetaAnalysisContainerController);
});
