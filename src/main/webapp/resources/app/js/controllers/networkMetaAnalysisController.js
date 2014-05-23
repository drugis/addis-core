'use strict';
define([], function() {
  var dependencies = ['$scope', '$q', '$state', '$stateParams', 'OutcomeResource', 'InterventionResource',
    'TrialverseTrialDataResource', 'NetworkMetaAnalysisService', 'ModelResource'
  ];

  var NetworkMetaAnalysisController = function($scope, $q, $state, $stateParams, OutcomeResource,
    InterventionResource, TrialverseTrialDataResource, NetworkMetaAnalysisService, ModelResource) {
    $scope.analysis = $scope.$parent.analysis;
    $scope.project = $scope.$parent.project;
    $scope.outcomes = OutcomeResource.query({
      projectId: $stateParams.projectId
    });
    $scope.trialData = {};
    $scope.interventions = InterventionResource.query({
      projectId: $stateParams.projectId
    });

    function matchOutcome(outcome) {
      return $scope.analysis.outcome && $scope.analysis.outcome.id === outcome.id;
    }

    function getSemanticInterventionUri(object) {
      return object.semanticInterventionUri;
    }

    function reloadTable() {
      TrialverseTrialDataResource
        .get({
          id: $scope.project.trialverseId,
          outcomeUri: $scope.analysis.outcome.semanticOutcomeUri,
          interventionUris: $scope.interventionUris
        })
        .$promise
        .then(NetworkMetaAnalysisService.transformTrialDataToTableRows)
        .then(function(tableRows) {
          $scope.trialData = tableRows;
        });
    }

    $q
      .all([
        $scope.analysis.$promise,
        $scope.project.$promise,
        $scope.outcomes.$promise,
        $scope.interventions.$promise
      ])
      .then(function() {
        $scope.interventionUris = _.map($scope.interventions, getSemanticInterventionUri);
        $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);
        if ($scope.analysis.outcome) {
          reloadTable();
        }
      });

    $scope.saveAnalysis = function() {
      $scope.analysis.$save(function() {
        $scope.analysis.outcome = _.find($scope.outcomes, matchOutcome);
        reloadTable();
      });
    };

    $scope.goToModel = function() {
      var model = ModelResource.save($stateParams, {}); 
      model.$promise.then(function(model) {
        $state.go('analysis.model', {
          modelId: model.id
        });
      });
    };

  };

  return dependencies.concat(NetworkMetaAnalysisController);
});