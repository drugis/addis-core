'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$stateParams', '$q', 'ModelService',
    'PataviService', 'RelativeEffectsTableService', 'CacheService', 'AnalysisService'
  ];
  var RelativeEffectsTableDirective = function($stateParams, $q, ModelService,
    PataviService, RelativeEffectsTableService, CacheService, AnalysisService) {
    return {
      restrict: 'E',
      scope: {
        analysisId: '=',
        modelId: '=',
        regressionLevel: '='
      },
      templateUrl: './relativeEffectsTableTemplate.html',

      link: function(scope) {
        scope.resultsMessage = {};

        function getResults(model) {
          return PataviService.listen(model.taskUrl);
        }

        var problemPromise = CacheService.getProblem($stateParams.projectId, scope.analysisId);
        var modelPromise = CacheService.getModel($stateParams.projectId, scope.analysisId, scope.modelId);

        $q.all([problemPromise, modelPromise]).then(function(values) {
          var problem = values[0];
          var model = values[1];
          scope.scaleName = AnalysisService.getScaleName(model);

          getResults(model).then(function(results) {
            scope.relativeEffectsTables = _.map(results.relativeEffects, function(relativeEffect, key) {
              return {
                level: key,
                table: RelativeEffectsTableService.buildTable(relativeEffect, problem.treatments, results.logScale)
              };
            });
            if (scope.regressionLevel !== undefined) {
              scope.relativeEffectsTable = _.find(scope.relativeEffectsTables, ['level', scope.regressionLevel.toString()]);
            } else if (model.regressor && ModelService.isVariableBinary(model.regressor.variable, problem)) {
              scope.relativeEffectsTables = ModelService.filterCentering(scope.relativeEffectsTables);
              scope.relativeEffectsTable = scope.relativeEffectsTables[0];
            }
            if (!scope.relativeEffectsTable) {
              scope.relativeEffectsTable = ModelService.findCentering(scope.relativeEffectsTables);
              if (model.regressor) {
                scope.relativeEffectsTable.level = 'centering (' + results.regressor.modelRegressor.mu + ')';
              }
            }
          });
        });
      }
    };
  };
  return dependencies.concat(RelativeEffectsTableDirective);
});
