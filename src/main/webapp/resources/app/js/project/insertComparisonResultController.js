'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$q', '$stateParams', '$modalInstance', 'AnalysisResource', 'ModelResource',
    'ReportDirectiveService', 'PataviService', 'InterventionResource', 'callback'
  ];
  var InsertComparisonResultController = function($scope, $q, $stateParams, $modalInstance, AnalysisResource, ModelResource,
    ReportDirectiveService, PataviService, InterventionResource, callback) {
    var analysesPromise = AnalysisResource.query($stateParams).$promise;

    var modelsPromise = ModelResource.getConsistencyModels($stateParams).$promise;

    var interventionPromise = InterventionResource.query($stateParams).$promise;

    $scope.selections = {};

    $scope.loading = {
      loaded: false
    };

    $q.all([analysesPromise, modelsPromise, interventionPromise]).then(function(values) {
      var analyses = values[0];
      var models = values[1];
      var interventions = $scope.interventions = _.keyBy(values[2], 'id');
      var modelResultsPromises = [];

      $scope.analyses = _.filter(analyses, ['analysisType', 'Evidence synthesis']);
      if ($scope.analyses.length) {
        $scope.selections.analysis = $scope.analyses[0];
      }

      models = _.filter(models, ['modelType.type', 'network']);

      models = _.map(models, function(model) {
        if (model.taskUrl) {
          var resultsPromise = PataviService.listen(model.taskUrl);
          modelResultsPromises.push(resultsPromise);
          resultsPromise.then(function(modelResults) {
            model.comparisons = _.map(modelResults.relativeEffects.centering, function(comparison) {
              return {
                label: interventions[comparison.t1].name + ' - ' + interventions[comparison.t2].name,
                t1: comparison.t1,
                t2: comparison.t2
              };
            });
          });
        }
        return model;
      });

      $q.all(modelResultsPromises).then(function() {
        models = _.filter(models, function(model) {
          return model.comparisons && model.comparisons.length;
        });
        $scope.analyses = _.map($scope.analyses, function(analysis) {
          analysis.models = _.filter(models, ['analysisId', analysis.id]);
          return analysis;
        });
        $scope.analyses = _.filter($scope.analyses, function(analysis) {
          return analysis.models.length > 0;
        });
        $scope.selectedAnalysisChanged();
        $scope.loading.loaded = true;
      });

    });

    function sortComparisons() {
        $scope.selections.model.comparisons = _.sortBy($scope.selections.model.comparisons, 'label');
    }

    $scope.selectedAnalysisChanged = function() {
      if ($scope.selections.analysis.primaryModel) {
        $scope.selections.model = _.find($scope.selections.analysis.models, ['id', $scope.selections.analysis.primaryModel]);
      } else {
        $scope.selections.model = $scope.selections.analysis.models[0];
      }
      $scope.selectedModelChanged();
    };

    $scope.selectedModelChanged = function() {
      if (!$scope.selections.model || !$scope.selections.model.comparisons) {
        return;
      }
      sortComparisons();
      $scope.selections.comparison = $scope.selections.model.comparisons[0];
    };

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };

    $scope.insertComparisonResult = function() {
      callback(ReportDirectiveService.getDirectiveBuilder('result-comparison')($scope.selections.analysis.id,
        $scope.selections.model.id, $scope.selections.comparison.t1, $scope.selections.comparison.t2));
      $modalInstance.close();
    };

    $scope.reverseComparison = function() {
      var comparison = $scope.selections.comparison;
      var tmp = comparison.t1;
      comparison.t1 = comparison.t2;
      comparison.t2 = tmp;
      comparison.label = $scope.interventions[comparison.t1].name + ' - ' + $scope.interventions[comparison.t2].name;
      sortComparisons();
    };

  };
  return dependencies.concat(InsertComparisonResultController);
});
