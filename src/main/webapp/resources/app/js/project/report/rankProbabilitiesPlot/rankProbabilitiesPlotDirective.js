'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$stateParams', '$q', 'ModelService',
    'PataviService', 'ResultsPlotService',
    'CacheService'
  ];
  var RankProbabilitiesPlotDirective = function($stateParams, $q, ModelService,
    PataviService, ResultsPlotService, CacheService) {
    return {
      restrict: 'E',
      scope: {
        analysisId: '=',
        modelId: '=',
        baselineTreatmentId: '=',
        regressionLevel: '='
      },
      templateUrl: './rankProbabilitiesPlotTemplate.html',
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

          getResults(model).then(function(results) {
            var rankProbabilitiesByLevel = ModelService.addLevelandProcessData(results.rankProbabilities,
            problem.treatments, ModelService.nameRankProbabilities);

            if (scope.regressionLevel !== undefined) {
              scope.rankProbabilities = _.find(rankProbabilitiesByLevel, ['level', scope.regressionLevel.toString()]);
            } else {
              var rankProbabilities = ModelService.selectLevel(model.regressor, problem, rankProbabilitiesByLevel,
                results.regressor);
              scope.rankProbabilities = rankProbabilities.selected;
            }
          });
        });
      }

    };
  };
  return dependencies.concat(RankProbabilitiesPlotDirective);
});
