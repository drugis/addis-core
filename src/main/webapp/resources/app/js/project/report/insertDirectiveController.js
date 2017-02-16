'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', '$q',
    'ReportDirectiveService', 'CacheService', 'PataviService', 'callback', 'directiveName'
  ];
  var InsertDirectiveController = function($scope, $stateParams, $modalInstance, $q,
    ReportDirectiveService, CacheService, PataviService, callback, directiveName) {
    var analysesPromise = CacheService.getAnalyses($stateParams);
    var modelsPromise = CacheService.getModelsByProject($stateParams);
    var interventionsPromise = CacheService.getInterventions($stateParams);

    $scope.selections = {};
    $scope.isRegressionModel = false;
    $scope.showSelectBaseline = false;
    $scope.showSelectInterventions = false;
    $scope.showSelectModel = true;
    $scope.showSelectRegression = true;
    $scope.selectedAnalysisChanged = selectedAnalysisChanged;
    $scope.selectedModelChanged = selectedModelChanged;
    $scope.selectedTreatmentChanged = selectedTreatmentChanged;
    $scope.insertDirective = insertDirective;

    $scope.loading = {
      loaded: false
    };

    $scope.title = directiveName.replace(/-/g, ' ');

    switch (directiveName) {
      case 'network-plot':
        $scope.showSelectModel = false;
        $scope.showSelectRegression = false;
        break;
      case 'comparison-result':
        $scope.showSelectRegression = false;
        $scope.showSelectInterventions = true;
        break;
      case 'relative-effects-table':
        break;
      case 'relative-effects-plot':
        $scope.showSelectBaseline = true;
        break;
      case 'rank-probabilities-table':
        break;
      case 'rank-probabilities-plot':
        $scope.showSelectBaseline = true;
        break;
      case 'forest-plot':
        $scope.showSelectRegression = false;
        break;
    }

    $q.all([analysesPromise, modelsPromise, interventionsPromise]).then(function(values) {
      var analyses = values[0];
      var models = values[1];
      $scope.interventions = values[2];
      if (directiveName === 'forest-plot') {
        models = ReportDirectiveService.getPairwiseModels(models);
      } else {
        models = ReportDirectiveService.getNonNodeSplitModels(models);
      }

      if (directiveName === 'comparison-result') {
        loadComparisonModelsAndAnalyses(models, analyses);
      } else {
        loadAnalyses(analyses, models);
      }
    });

    function loadAnalyses(analyses, models) {
      $scope.analyses = ReportDirectiveService.getDecoratedSyntheses(analyses, models, $scope.interventions);

      if (analyses.length) {
        $scope.selections.analysis = analyses[0];
      }
      if (directiveName !== 'network-plot') {
        $scope.selectedAnalysisChanged();
      }
      $scope.loading.loaded = true;
    }

    function loadComparisonModelsAndAnalyses(models, analyses) {
      var interventionsById = _.chain($scope.interventions)
        .sortBy('name')
        .keyBy('id')
        .value();
      var modelResultsPromises = [];

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
        loadAnalyses(analyses, models);
      });
    }


    function selectedAnalysisChanged() {
      if (!$scope.selections.analysis) {
        return;
      }
      if ($scope.selections.analysis.primaryModel) {
        $scope.selections.model = _.find($scope.selections.analysis.models, ['id', $scope.selections.analysis.primaryModel]);
      } else {
        $scope.selections.model = $scope.selections.analysis.models[0];
      }
      if (directiveName === 'relative-effects-plot' || directiveName === 'rank-probabilities-plot') {
        $scope.selections.baselineIntervention = $scope.selections.analysis.interventions[0];
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
      if ($scope.showSelectInterventions) {
        if (!$scope.selections.model.comparisons) {
          return;
        }
        $scope.selections.t1 = $scope.interventions[0];
        selectedTreatmentChanged();
      }
    }

    function selectedTreatmentChanged() {
      $scope.secondInterventionOptions = _.reject($scope.interventions, ['id', $scope.selections.t1.id]);
      $scope.selections.t2 = $scope.secondInterventionOptions[0];
    }
    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };

    function insertDirective() {
      switch (directiveName) {
        case 'network-plot':
          callback(ReportDirectiveService.getDirectiveBuilder(directiveName)($scope.selections.analysis.id));
          $modalInstance.close();
          break;
        case 'comparison-result':
          callback(ReportDirectiveService.getDirectiveBuilder('result-comparison')($scope.selections.analysis.id,
            $scope.selections.model.id, $scope.selections.t1.id, $scope.selections.t2.id));
          $modalInstance.close();
          break;
        case 'relative-effects-table':
          callback(ReportDirectiveService.getDirectiveBuilder(directiveName)($scope.selections.analysis.id,
            $scope.selections.model.id, $scope.selections.regressionLevel));
          $modalInstance.close();
          break;
        case 'relative-effects-plot':
          callback(ReportDirectiveService.getDirectiveBuilder(directiveName)($scope.selections.analysis.id,
            $scope.selections.model.id, $scope.selections.baselineIntervention.id, $scope.selections.regressionLevel));
          $modalInstance.close();
          break;
        case 'rank-probabilities-table':
          callback(ReportDirectiveService.getDirectiveBuilder(directiveName)($scope.selections.analysis.id,
            $scope.selections.model.id, $scope.selections.regressionLevel));
          $modalInstance.close();
          break;
        case 'rank-probabilities-plot':
          callback(ReportDirectiveService.getDirectiveBuilder(directiveName)($scope.selections.analysis.id,
            $scope.selections.model.id, $scope.selections.baselineIntervention.id, $scope.selections.regressionLevel));
          $modalInstance.close();
          break;
        case 'forest-plot':
          callback(ReportDirectiveService.getDirectiveBuilder(directiveName)($scope.selections.analysis.id, $scope.selections.model.id));
          $modalInstance.close();
      }
    }
  };
  return dependencies.concat(InsertDirectiveController);
});
