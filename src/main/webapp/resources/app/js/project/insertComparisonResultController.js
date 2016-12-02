'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$q', '$stateParams', '$modalInstance', 'AnalysisResource', 'ModelResource',
    'ReportDirectiveService', 'PataviService', 'InterventionResource', 'callback'];
  var InsertComparisonResultController = function($scope, $q, $stateParams, $modalInstance, AnalysisResource, ModelResource,
    ReportDirectiveService, PataviService, InterventionResource, callback) {
    var analysesPromise = AnalysisResource.query($stateParams).$promise;

    var modelsPromise = ModelResource.getConsistencyModels($stateParams).$promise;

    var interventionPromise = InterventionResource.query($stateParams).$promise;

    $scope.selections = {};

    $q.all([analysesPromise, modelsPromise, interventionPromise]).then(function(values) {
      var analyses = values[0];
      var models = values[1];
      var interventions = _.keyBy(values[2], 'id');

      $scope.analyses = _.filter(analyses, ['analysisType', 'Evidence synthesis']);
      if ($scope.analyses.length) {
        $scope.selections.analysis = $scope.analyses[0];
      }

      models = _.map(models, function(model) {
        if (model.taskUrl) {
          PataviService.listen(model.taskUrl).then(function(modelResults) {
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

      $scope.analyses = _.map($scope.analyses, function(analysis) {
        analysis.models = _.filter(models, ['analysisId', analysis.id]);
        return analysis;
      });
    });

    $scope.selectedAnalysisChanged = function() {
      $scope.selections.model = $scope.selections.analysis.models[0];
      $scope.selectedModelChanged();
    };

    $scope.selectedModelChanged = function() {
      if (!$scope.selections.model) {
        return;
      }
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

  };
  return dependencies.concat(InsertComparisonResultController);
});
