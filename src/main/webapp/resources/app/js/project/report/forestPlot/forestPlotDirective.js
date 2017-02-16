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
      templateUrl: 'app/js/project/report/forestPlot/forestPlotTemplate.html',
      link: function(scope) {
        scope.resultsMessage = {};

        function getResults(model) {
          return PataviService.listen(model.taskUrl);
        }

        var modelPromise = CacheService.getModel($stateParams.projectId, scope.analysisId, scope.modelId);
        $q.all([modelPromise]).then(function(values) {
          var model = values[0];
          getResults(model).then(function(results) {
            scope.results.studyForestPlot = _.map(results.studyForestPlot, function(page) {
              return {
                href: model.taskUrl + '/results/' + page.href,
                'content-type': page['content-type']
              };
            });
          });
        });
      }

    };
  };
  return dependencies.concat(RankProbabilitiesPlotDirective);
});
