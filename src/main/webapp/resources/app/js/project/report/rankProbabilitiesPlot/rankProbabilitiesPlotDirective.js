'use strict';
define(['lodash', 'd3', 'nvd3'], function(_, d3, nv) {
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
      link: function(scope, element) {
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
            d3.selectAll('svg > *').remove();

            nv.addGraph(function() {
              svg.append('rect')
                .attr('width', '100%')
                .attr('height', '100%')
                .attr('fill', 'white');

              var chart = nv.models.multiBarChart().height(dim.height).width(dim.width);
              var data = rankGraphData(scope.rankProbabilities.data);

              chart.yAxis.tickFormat(d3.format(',.3f'));
              chart.reduceXTicks(false);
              chart.staggerLabels(true);
              chart.tooltips(false);

              svg.datum(data)
                .transition().duration(100).call(chart);

              nv.utils.windowResize(chart.update);
            });
          });
        });
        
        var svg = d3.select(element[0]).append('svg')
          .attr('width', '100%')
          .attr('height', '100%')
          .style('font-size', '12px');

        scope.$on('$locationChangeStart', function() {
          window.onresize = null;
        });

        function parsePx(str) {
          return parseInt(str.replace(/px/gi, ''));
        }

        var getParentDimension = function(element) {
          var width = parsePx($(element[0].parentNode).css('width'));
          var height = parsePx($(element[0].parentNode).css('height'));

          return {
            width: width,
            height: height
          };
        };

        var dim = getParentDimension(element);

        var rankGraphData = function(data) {
          var result = [];
          _.forEach(_.toPairs(data), function(el) {
            var key = el[0];
            var values = el[1];
            for (var i = 0; i < values.length; i++) {
              var obj = result[i] || {
                key: 'Rank ' + (i + 1),
                values: []
              };
              obj.values.push({
                x: key,
                y: values[i]
              });
              result[i] = obj;
            }
          });
          return result;
        };
      }
    };
  };
  return dependencies.concat(RankProbabilitiesPlotDirective);
});
