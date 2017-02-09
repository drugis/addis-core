'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', '$q', 'AnalysisResource',
    'ReportDirectiveService', 'callback', 'ModelResource'
  ];
  var InsertRelativeEffectsTableController = function($scope, $stateParams, $modalInstance, $q, AnalysisResource,
    ReportDirectiveService, callback, ModelResource) {
    var analysesPromise = AnalysisResource.query($stateParams).$promise;
    var modelsPromise = ModelResource.queryByProject($stateParams).$promise;

    $scope.selections = {};
    $scope.isRegressionModel = false;
    $scope.selectedAnalysisChanged = selectedAnalysisChanged;
    $scope.selectedModelChanged = selectedModelChanged;
    $scope.insertRelativeEffectsTable = insertRelativeEffectsTable;
    $scope.loading = {
      loaded: false
    };

    $q.all([analysesPromise, modelsPromise]).then(function(values) {
      var analyses = values[0];
      var models = values[1];
      models = _.chain(models).reject('archived').reject(['modelType.type', 'node-split']).value();
      $scope.analyses = _.chain(analyses)
        .reject('archived')
        .filter(['analysisType', 'Evidence synthesis'])
        .map(function(analysis) {
          analysis.models = _.filter(models, ['analysisId', analysis.id]);
          return analysis;
        })
        .filter('models.length')
        .value();
      if ($scope.analyses.length) {
        $scope.selections.analysis = $scope.analyses[0];
      }
      $scope.selectedAnalysisChanged();
      $scope.loading.loaded = true;
    });

    function selectedAnalysisChanged() {
      if (!$scope.selections.analysis) {
        return;
      }
      if ($scope.selections.analysis.primaryModel) {
        $scope.selections.model = _.find($scope.selections.analysis.models, ['id', $scope.selections.analysis.primaryModel]);
      } else {
        $scope.selections.model = $scope.selections.analysis.models[0];
      }
      selectedModelChanged();
    }

    function selectedModelChanged() {
      if (!$scope.selections.model) {
        return;
      }
      $scope.isRegressionModel = $scope.selections.model.modelType.type === 'regression';
      if ($scope.isRegressionModel && $scope.selections.model.regressor.levels.length) {
        $scope.selections.regressionLevel = $scope.selections.model.regressor.levels[0];
      } else {
        delete $scope.selections.regressionLevel;
      }
    }

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };

    function insertRelativeEffectsTable() {
      if ($scope.selections.regressionLevel) {
        callback(ReportDirectiveService.getDirectiveBuilder('relative-effects-table')($scope.selections.analysis.id,
          $scope.selections.model.id, $scope.selections.regressionLevel));
      } else {
        callback(ReportDirectiveService.getDirectiveBuilder('relative-effects-table')($scope.selections.analysis.id,
          $scope.selections.model.id));
      }
      $modalInstance.close();
    }
  };
  return dependencies.concat(InsertRelativeEffectsTableController);
});
