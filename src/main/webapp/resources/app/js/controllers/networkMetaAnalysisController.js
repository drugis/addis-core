define([], function() {
  var dependencies = ['$scope', '$q', '$stateParams', 'OutcomeResource', 'InterventionResource','TrialverseTrialDataResource'];

  var NetworkMetaAnalysisController = function($scope, $q, $stateParams, OutcomeResource, InterventionResource, TrialverseTrialDataResource) {
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

    function transformTrialDataToTableRows (studies) {
      var tableRows = [];
      angular.forEach(studies, function(study){
        angular.forEach(study.trialDataInterventions, function(intervention, index){
          var row = {};
          if(index === 0) {
            row.study = study.title;
            row.rowSpan = study.trialDataInterventions.length;
          }
          row.intervention = intervention.drugId;
          tableRows.push(row);
        });
      });
      return tableRows;
    }

    function reloadTable() {
      TrialverseTrialDataResource
        .get({
          id: $scope.project.trialverseId,
          outcomeUri: $scope.analysis.outcome.semanticOutcomeUri,
          interventionUris: $scope.interventionUris
        })
        .$promise
        .then(function(trialData){
          $scope.trialData = transformTrialDataToTableRows(trialData.studies);
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
  };

  return dependencies.concat(NetworkMetaAnalysisController);
});