'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$q', '$stateParams', '$modalInstance', 'CacheService',
    'ReportDirectiveService', 'PataviService', 'callback'
  ];
  var InsertComparisonResultController = function($scope, $q, $stateParams, $modalInstance, CacheService,
    ReportDirectiveService, PataviService, callback) {

    $scope.selections = {};
    $scope.loading = {
      loaded: false
    };
    $scope.insertComparisonResult = insertComparisonResult;
    $scope.selectedAnalysisChanged = selectedAnalysisChanged;
    $scope.selectedModelChanged = selectedModelChanged;
    $scope.treatmentSelectionChanged = treatmentSelectionChanged;

    var analysesPromise = CacheService.getAnalyses($stateParams);
    var modelsPromise = CacheService.getConsistencyModels($stateParams);
    var interventionPromise = CacheService.getInterventions($stateParams);

    $q.all([analysesPromise, modelsPromise, interventionPromise]).then(function(values) {
      var analyses = values[0];
      var models = values[1];
      $scope.interventions = _.sortBy(values[2], 'name');
      var interventionsById = _.keyBy($scope.interventions, 'id');
      var modelResultsPromises = [];
      $scope.analyses = _.chain(analyses).reject('archived').filter(['analysisType', 'Evidence synthesis']).value();
      if ($scope.analyses.length) {
        $scope.selections.analysis = $scope.analyses[0];
      }

      models = _.chain(models).reject('archived').filter(['modelType.type', 'network']).value();

      models = _.map(models, function(model) {
        if (model.taskUrl) {
          var resultsPromise = PataviService.listen(model.taskUrl);
          modelResultsPromises.push(resultsPromise);
          resultsPromise.then(function(modelResults) {
            model.comparisons = _.map(modelResults.relativeEffects.centering, function(comparison) {
              return {
                label: interventionsById[comparison.t1].name + ' - ' + interventionsById[comparison.t2].name,
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
        if($scope.analyses.length) {
          $scope.selections.analysis = $scope.analyses[0];
        }
        $scope.selectedAnalysisChanged();
        $scope.loading.loaded = true;
      });
    });

    function selectedAnalysisChanged() {
      if(!$scope.selections.analysis){
        return;
      }
      if($scope.selections.analysis.primaryModel) {
        $scope.selections.model = _.find($scope.selections.analysis.models, ['id', $scope.selections.analysis.primaryModel]);
      } else {
        $scope.selections.model = $scope.selections.analysis.models[0];
      }
      $scope.selectedModelChanged();
    }

    function selectedModelChanged() {
      if (!$scope.selections.model || !$scope.selections.model.comparisons) {
        return;
      }
      $scope.selections.t1 = $scope.interventions[0];
      treatmentSelectionChanged();
    }


    function insertComparisonResult() {
      callback(ReportDirectiveService.getDirectiveBuilder('result-comparison')($scope.selections.analysis.id,
        $scope.selections.model.id, $scope.selections.t1.id, $scope.selections.t2.id));
      $modalInstance.close();
    }

    function treatmentSelectionChanged() {
      $scope.secondInterventionOptions = _.reject($scope.interventions, ['id', $scope.selections.t1.id]);
      $scope.selections.t2 = $scope.secondInterventionOptions[0];
    }

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };
  return dependencies.concat(InsertComparisonResultController);
});
