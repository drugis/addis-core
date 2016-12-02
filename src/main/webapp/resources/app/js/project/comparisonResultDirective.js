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
      template: '<span>{{median | number:3}} ({{lowerBound | number:3}}, {{upperBound | number:3}})</span>',
      link: function(scope) {

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
            var relEffects = _.find(results.relativeEffects.centering, function(relEffect) {
              return relEffect.t1 === scope.t1.toString() && relEffect.t2 === scope.t2.toString();
            });
            scope.median = relEffects.quantiles['50%'];
            scope.lowerBound = relEffects.quantiles['2.5%'];
            scope.upperBound = relEffects.quantiles['97.5%'];
          });
      }
    };
  };
  return dependencies.concat(ComparisonResultDirective);
});
