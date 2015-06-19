'use strict';
define(['d3'], function(d3) {
  var dependencies = ['$scope', '$q', '$state', '$stateParams', 'OutcomeResource', 'InterventionResource',
    'TrialverseTrialDataResource', 'NetworkMetaAnalysisService', 'ModelResource'
  ];

  var NetworkMetaAnalysisController = function($scope, $q, $state, $stateParams, OutcomeResource,
    InterventionResource, TrialverseTrialDataResource, NetworkMetaAnalysisService, ModelResource) {
    $scope.networkGraph = {};
    $scope.isNetworkDisconnected = true;
    $scope.analysis = $scope.$parent.analysis;
    $scope.project = $scope.$parent.project;
    $scope.models = ModelResource.query({
      projectId: $stateParams.projectId,
      analysisId: $stateParams.analysisId
    });
    $scope.outcomes = OutcomeResource.query({
      projectId: $stateParams.projectId
    });
    $scope.trialData = {};
    $scope.interventions = InterventionResource.query({
      projectId: $stateParams.projectId
    });
    $scope.tableHasAmbiguousArm = false;
    $scope.hasLessThanTwoInterventions = false;
    $scope.hasModel = true;

    $q
      .all([
        $scope.analysis.$promise,
        $scope.project.$promise,
        $scope.models.$promise,
        $scope.outcomes.$promise,
        $scope.interventions.$promise
      ])
      .then(function() {
        $scope.hasModel = $scope.models.length > 0;
        $scope.interventions = NetworkMetaAnalysisService.addInclusionsToInterventions($scope.interventions, $scope.analysis.includedInterventions);
        $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);
        if ($scope.analysis.outcome) {
          reloadModel();
        }
      });

    function matchOutcome(outcome) {
      return $scope.analysis.outcome && $scope.analysis.outcome.id === outcome.id;
    }

    function addIncludedInterventionUri(memo, intervention) {
      if (intervention.isIncluded) {
        memo.push(intervention.semanticInterventionUri);
      }
      return memo;
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

    function reloadModel() {
      var includedInterventionUris = _.reduce($scope.interventions, addIncludedInterventionUri, []);
      TrialverseTrialDataResource
        .get({
          namespaceUid: $scope.project.namespaceUid,
          outcomeUri: $scope.analysis.outcome.semanticOutcomeUri,
          interventionUris: includedInterventionUris,
          version: $scope.project.datasetVersion
        })
        .$promise
        .then(function(trialverseData) {
          $scope.trialverseData = trialverseData;
          updateNetwork();
          var includedInterventions = getIncludedInterventions($scope.interventions);
          $scope.trialData = NetworkMetaAnalysisService.transformTrialDataToTableRows(trialverseData, includedInterventions, $scope.analysis.excludedArms);
          $scope.tableHasAmbiguousArm =
            NetworkMetaAnalysisService.doesModelHaveAmbiguousArms($scope.trialverseData, $scope.interventions, $scope.analysis);
          $scope.hasLessThanTwoInterventions = getIncludedInterventions($scope.interventions).length < 2;
        });
    }

    $scope.changeArmExclusion = function(dataRow) {
      $scope.tableHasAmbiguousArm = false;
      $scope.analysis = NetworkMetaAnalysisService.changeArmExclusion(dataRow, $scope.analysis);
      updateNetwork();
      $scope.analysis.$save(function() {
        $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);
        $scope.tableHasAmbiguousArm =
          NetworkMetaAnalysisService.doesModelHaveAmbiguousArms($scope.trialverseData, $scope.interventions, $scope.analysis);
      });
    };

    $scope.lessThanTwoInterventionArms = function(dataRow) {
      var matchedAndIncludedRows = _.filter(dataRow.studyRows, function(studyRow) {
        return studyRow.intervention !== 'unmatched' && studyRow.included;
      });
      var matchedInterventions = _.uniq(_.pluck(matchedAndIncludedRows, 'intervention'));
      return matchedInterventions.length < 2;
    };

    $scope.changeInterventionInclusion = function(intervention) {
      $scope.analysis.includedInterventions =
        NetworkMetaAnalysisService.buildInterventionInclusions($scope.interventions, $scope.analysis);
      if ($scope.trialverseData && !intervention.isIncluded) {
        $scope.analysis.excludedArms = NetworkMetaAnalysisService.cleanUpExcludedArms(intervention, $scope.analysis, $scope.trialverseData);
      }
      $scope.analysis.$save(function() {
        $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);
        $scope.tableHasAmbiguousArm =
          NetworkMetaAnalysisService.doesModelHaveAmbiguousArms($scope.trialverseData, $scope.interventions, $scope.analysis);
        reloadModel();
      });
    };

    $scope.changeSelectedOutcome = function() {
      $scope.tableHasAmbiguousArm = false;
      $scope.analysis.excludedArms = [];
      $scope.analysis.$save(function() {
        $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);
        reloadModel();
      });
    };

    $scope.createModelAndGoToModel = function() {
      var model = ModelResource.save($stateParams, {});
      model.$promise.then(function(model) {
        $state.go('analysis.model', {
          modelId: model.id
        });
      });
    };

    $scope.goToModel = function() {
      $state.go('analysis.model', {
        modelId: $scope.models[0].id
      });
    };

    $scope.doesInterventionHaveAmbiguousArms = function(drugId, studyUid) {
      return NetworkMetaAnalysisService.doesInterventionHaveAmbiguousArms(drugId, studyUid, $scope.trialverseData, $scope.analysis);
    };

  };

  return dependencies.concat(NetworkMetaAnalysisController);
});