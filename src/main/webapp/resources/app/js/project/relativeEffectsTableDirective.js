'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$stateParams', '$q', 'ModelResource', 'ModelService',
    'PataviService', 'RelativeEffectsTableService', 'ProblemResource', 'AnalysisService'
  ];
  var RelativeEffectsTableDirective = function($stateParams, $q, ModelResource, ModelService,
    PataviService, RelativeEffectsTableService, ProblemResource, AnalysisService) {
    return {
      restrict: 'E',
      scope: {
        analysisId: '=',
        modelId: '=',
        regressionLevel: '='
      },
      templateUrl: 'app/js/project/relativeEffectsTableTemplate.html',

      link: function(scope) {
        scope.resultsMessage = {};

        function getResults(model) {
          return PataviService.listen(model.taskUrl);
        }
        var problemPromise = ProblemResource.get({
          analysisId: scope.analysisId,
          projectId: $stateParams.projectId
        }).$promise;


        var modelPromise = ModelResource.get({
          projectId: $stateParams.projectId,
          analysisId: scope.analysisId,
          modelId: scope.modelId
        }).$promise;

        $q.all([problemPromise, modelPromise]).then(function(values) {
          scope.problem = values[0];
          scope.model = values[1];
          scope.scaleName = AnalysisService.getScaleName(scope.model);

          getResults(scope.model).then(function(results) {
            scope.result = results;
            scope.relativeEffectsTables = _.map(results.relativeEffects, function(relativeEffect, key) {
              return {
                level: key,
                table: RelativeEffectsTableService.buildTable(relativeEffect, results.logScale, scope.problem.treatments)
              };
            });
            if (scope.regressionLevel) {
              scope.relativeEffectsTable = _.find(scope.relativeEffectsTables, ['level', scope.regressionLevel.toString()]);
            } else if (scope.model.regressor && ModelService.isVariableBinary(scope.model.regressor.variable, scope.problem)) {
              scope.relativeEffectsTables = ModelService.filterCentering(scope.relativeEffectsTables);
              scope.relativeEffectsTable = scope.relativeEffectsTable = scope.relativeEffectsTables[0];
            }
            if (!scope.relativeEffectsTable) {
              scope.relativeEffectsTable = ModelService.findCentering(scope.relativeEffectsTables);
              if (scope.model.regressor) {
                scope.relativeEffectsTable.level = 'centering (' + scope.result.regressor.modelRegressor.mu + ')';
              }
            }
          });
        });
      }
    };
  };
  return dependencies.concat(RelativeEffectsTableDirective);
});
