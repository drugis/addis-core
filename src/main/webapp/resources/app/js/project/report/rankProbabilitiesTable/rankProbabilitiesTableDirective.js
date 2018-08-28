'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$stateParams', '$q', 'ModelService',
    'PataviService', 'CacheService'
  ];
  var RankProbabilitiesTableDirective = function($stateParams, $q, ModelService,
    PataviService, CacheService) {
    return {
      restrict: 'E',
      scope: {
        analysisId: '=',
        modelId: '=',
        regressionLevel: '='
      },
      templateUrl: './rankProbabilitiesTableTemplate.html',

      link: function(scope) {
        scope.resultsMessage = {};

        function getResults(model) {
          return PataviService.listen(model.taskUrl);
        }

        function nameRankProbabilities(rankProbabilities, treatments) {
          return _.reduce(_.toPairs(rankProbabilities), function(memo, pair) {
            var treatmentName = _.find(treatments, function(treatment) {
              return treatment.id.toString() === pair[0];
            }).name;
            memo[treatmentName] = pair[1];
            return memo;
          }, {});
        }

        var problemPromise = CacheService.getProblem($stateParams.projectId, scope.analysisId);
        var modelPromise = CacheService.getModel($stateParams.projectId, scope.analysisId, scope.modelId);

        $q.all([problemPromise, modelPromise]).then(function(values) {
          var problem = values[0];
          var model = values[1];

          getResults(model).then(function(results) {
            scope.rankProbabilitiesByLevel = _.map(results.rankProbabilities, function(rankProbability, key) {
              return {
                level: key,
                data: nameRankProbabilities(rankProbability, problem.treatments)
              };
            });

            if (scope.regressionLevel !== undefined) {
              scope.rankProbabilities = _.find(scope.rankProbabilitiesByLevel, ['level', scope.regressionLevel.toString()]);
            } else if (model.regressor && ModelService.isVariableBinary(model.regressor.variable, problem)) {
              scope.rankProbabilitiesByLevel = ModelService.filterCentering(scope.rankProbabilitiesByLevel);
              scope.rankProbabilities = scope.rankProbabilitiesByLevel[0];
            }
            if (!scope.rankProbabilities) {
              scope.rankProbabilities = ModelService.findCentering(scope.rankProbabilitiesByLevel);
              if (model.regressor) {
                scope.rankProbabilities.level = 'centering (' + results.regressor.modelRegressor.mu + ')';
              }
            }
          });
        });
      }
    };
  };
  return dependencies.concat(RankProbabilitiesTableDirective);
});
