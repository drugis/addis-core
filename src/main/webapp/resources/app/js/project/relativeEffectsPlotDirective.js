'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$stateParams', '$q', 'ModelResource', 'ModelService',
    'PataviService', 'ProblemResource', 'AnalysisService','ResultsPlotService'
  ];
  var RelativeEffectsPlotDirective = function($stateParams, $q, ModelResource, ModelService,
    PataviService, ProblemResource, AnalysisService,ResultsPlotService) {
    return {
      restrict: 'E',
      scope: {
        analysisId: '=',
        modelId: '=',
        baselineTreatmentId: '=',
        regressionLevel: '='
      },
      templateUrl: 'app/js/project/relativeEffectsPlotTemplate.html',

      link: function(scope) {
        scope.resultsMessage = {};

        function getResults(model) {
          return PataviService.listen(model.taskUrl);
        }
        var problemPromise = ProblemResource.get({
          analysisId: scope.analysisId,
          projectId: $stateParams.projectId
        }).$promise;

        function prefixPlots(plots) {
          return _.reduce(plots, function(accum, plot, key) {
            accum[key] = ResultsPlotService.prefixImageUris(plot, scope.model.taskUrl + '/results/');
            return accum;
          }, {});
        }

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

            if (scope.problem.treatments && scope.problem.treatments.length > 0) {
              scope.selectedBaseline = _.find(scope.problem.treatments, ['id',scope.baselineTreatmentId]);
            }

            scope.relativeEffectsPlots = _.map(results.relativeEffectPlots, function(plots, key) {
              return {
                level: key,
                plots: prefixPlots(plots)
              };
            });

            if (scope.regressionLevel) {
              scope.relativeEffectsPlot = _.find(scope.relativeEffectsPlots, ['level', scope.regressionLevel.toString()]);
            } else if (scope.model.regressor && ModelService.isVariableBinary(scope.model.regressor.variable, scope.problem)) {
              scope.relativeEffectsPlots = ModelService.filterCentering(scope.relativeEffectsPlots);
              scope.relativeEffectsPlot = scope.relativeEffectsPlot = scope.relativeEffectsPlots[0];
            }
            if (!scope.relativeEffectsPlot) {
              scope.relativeEffectsPlot = ModelService.findCentering(scope.relativeEffectsPlots);
              if (scope.model.regressor) {
                scope.relativeEffectsPlot.level = 'centering (' + scope.result.regressor.modelRegressor.mu + ')';
              }
            }
          });
        });
      }
    };
  };
  return dependencies.concat(RelativeEffectsPlotDirective);
});
