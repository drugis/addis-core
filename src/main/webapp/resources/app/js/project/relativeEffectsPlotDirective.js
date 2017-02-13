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

        function prefixPlots(model, plots) {
          return _.reduce(plots, function(accum, plot, key) {
            accum[key] = ResultsPlotService.prefixImageUris(plot, model.taskUrl + '/results/');
            return accum;
          }, {});
        }

        var modelPromise = ModelResource.get({
          projectId: $stateParams.projectId,
          analysisId: scope.analysisId,
          modelId: scope.modelId
        }).$promise;

        $q.all([problemPromise, modelPromise]).then(function(values) {
          var problem = values[0];
          var model = values[1];

          getResults(model).then(function(results) {

            if (problem.treatments && problem.treatments.length > 0) {
              scope.selectedBaseline = _.find(problem.treatments, ['id',scope.baselineTreatmentId]);
            }

            scope.relativeEffectsPlots = _.map(results.relativeEffectPlots, function(plots, key) {
              return {
                level: key,
                plots: prefixPlots(model, plots)
              };
            });

            if (scope.regressionLevel !== undefined) {
              scope.relativeEffectsPlot = _.find(scope.relativeEffectsPlots, ['level', scope.regressionLevel.toString()]);
            } else if (model.regressor && ModelService.isVariableBinary(model.regressor.variable, problem)) {
              scope.relativeEffectsPlot = scope.relativeEffectsPlot = scope.relativeEffectsPlots[0];
            }
            if (!scope.relativeEffectsPlot) {
              scope.relativeEffectsPlot = ModelService.findCentering(scope.relativeEffectsPlots);
              if (model.regressor) {
                scope.relativeEffectsPlot.level = 'centering (' + results.regressor.modelRegressor.mu + ')';
              }
            }
          });
        });
      }
    };
  };
  return dependencies.concat(RelativeEffectsPlotDirective);
});
