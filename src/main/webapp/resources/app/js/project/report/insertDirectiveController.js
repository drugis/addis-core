'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', '$q',
    'ReportDirectiveService', 'CacheService', 'PataviService', 'callback', 'directiveName'
  ];
  var InsertDirectiveController = function($scope, $stateParams, $modalInstance, $q,
    ReportDirectiveService, CacheService, PataviService, callback, directiveName) {
    $scope.loading = {
      loaded: false
    };
    $scope.selections = {};
    $scope.isRegressionModel = false;
    $scope.title = directiveName.replace(/-/g, ' ');
    $scope.showSettings = ReportDirectiveService.getShowSettings(directiveName);
    $scope.selectedAnalysisChanged = selectedAnalysisChanged;
    $scope.selectedModelChanged = selectedModelChanged;
    $scope.selectedTreatmentChanged = selectedTreatmentChanged;
    $scope.insertDirective = insertDirective;
    $scope.sortOptions = ['alfabetical','point estimate'];

    var analysesPromise = CacheService.getAnalyses($stateParams);
    var modelsPromise = CacheService.getModelsByProject($stateParams);
    var interventionsPromise = CacheService.getInterventions($stateParams);

    $q.all([analysesPromise, modelsPromise, interventionsPromise]).then(function(values) {
      var analyses = values[0];
      var models = values[1];
      $scope.interventions = _.sortBy(values[2], 'name');
      models = ReportDirectiveService.getAllowedModels(models, directiveName);

      if(directiveName === 'treatment-effects'){
        $scope.selections.sortingType = $scope.sortOptions[0];
      }
      if (directiveName === 'comparison-result') {
        loadComparisonModelsAndAnalyses(models, analyses);
      } else {
        loadAnalyses(analyses, models);
      }
    });

    function loadAnalyses(analyses, models) {
      $scope.analyses = ReportDirectiveService.getDecoratedSyntheses(analyses, models, $scope.interventions);
      if ($scope.analyses.length) {
        $scope.selections.analysis = $scope.analyses[0];
      }
      if (directiveName !== 'network-plot') {
        $scope.selectedAnalysisChanged();
      }
      $scope.loading.loaded = true;
    }

    function loadComparisonModelsAndAnalyses(models, analyses) {
      var interventionsById = _.keyBy($scope.interventions, 'id');
      var modelResultsPromises = [];

      var modelsWithComparisons = _.map(models, function(model) {
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
        var filteredModels = _.filter(modelsWithComparisons, function(model) {
          return model.comparisons && model.comparisons.length;
        });
        loadAnalyses(analyses, filteredModels);
      });
    }

    function selectedAnalysisChanged() {
      if (!$scope.selections.analysis) {
        return;
      }
      $scope.selections.model = _.find($scope.selections.analysis.models, ['id', $scope.selections.analysis.primaryModel]) || $scope.selections.analysis.models[0];

      if (directiveName === 'relative-effects-plot' || directiveName === 'rank-probabilities-plot' || 'treatment-effects') {
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
      if (directiveName === 'comparison-result') {
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
      callback(ReportDirectiveService.getDirectiveBuilder(directiveName)($scope.selections));
      $modalInstance.close();
    }
  };
  return dependencies.concat(InsertDirectiveController);
});
