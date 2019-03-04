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
        modelId: '='
      },
      templateUrl: './forestPlotTemplate.html',
      link: function(scope) {
        var modelCache;
        CacheService.getModel($stateParams.projectId, scope.analysisId, scope.modelId)
          .then(function(model) {
            modelCache = model;
            return model.taskUrl;
          })
          .then(PataviService.listen)
          .then(function(results) {
            scope.studyForestPlot = _.map(results.studyForestPlot, function(page) {
              return {
                href: modelCache.taskUrl + '/results/' + page.href,
                'content-type': page['content-type']
              };
            });
          });
      }
    };
  };
  return dependencies.concat(RankProbabilitiesPlotDirective);
});
