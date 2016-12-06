'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$stateParams', 'ModelResource', 'PataviService'];
  var ComparisonResultDirective = function($stateParams, ModelResource, PataviService) {
    return {
      restrict: 'E',
      scope: {
        analysisId: '=',
        modelId: '=',
        t1: '=',
        t2: '='
      },
      template: '<span ng-if="!resultsMessage.text">{{median | number:3}} ({{lowerBound | number:3}}, {{upperBound | number:3}})</span>{{resultsMessage.text}}',
      link: function(scope) {
        scope.resultsMessage = {};
        function getResults(model) {
          return PataviService.listen(model.taskUrl);
        }

        ModelResource.get({
            projectId: $stateParams.projectId,
            analysisId: scope.analysisId,
            modelId: scope.modelId
          }).$promise
          .then(getResults)
          .then(function(results) {
            var inversionFactor = 1;
            var relEffects = _.find(results.relativeEffects.centering, function(relEffect) {
              return relEffect.t1 === scope.t1.toString() && relEffect.t2 === scope.t2.toString();
            });
            if (!relEffects) {
              //interventions inverted
              relEffects = _.find(results.relativeEffects.centering, function(relEffect) {
                return relEffect.t2 === scope.t1.toString() && relEffect.t1 === scope.t2.toString();
              });
              inversionFactor = -1;
            }
            if (relEffects) {
              scope.median = results.logScale ?
                Math.exp(inversionFactor * relEffects.quantiles['50%']) :
                inversionFactor * relEffects.quantiles['50%'];
              scope.lowerBound = results.logScale ?
                Math.exp(inversionFactor * relEffects.quantiles['2.5%']) :
                inversionFactor * relEffects.quantiles['2.5%'];
              scope.upperBound = results.logScale ?
                Math.exp(inversionFactor * relEffects.quantiles['97.5%']) :
                inversionFactor * relEffects.quantiles['97.5%'];
              if (inversionFactor === -1) {
                var tmp = scope.upperBound;
                scope.upperBound = scope.lowerBound;
                scope.lowerBound = tmp;
              }
            } else {
              scope.resultsMessage.text = 'No results for comparison';
            }
          });
      }
    };
  };
  return dependencies.concat(ComparisonResultDirective);
});
