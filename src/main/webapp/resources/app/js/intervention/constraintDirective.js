'use strict';
define(['angular'], function() {
  var dependencies = ['$stateParams', 'DosageService', 'InterventionService', 'ScaledUnitResource'];
  var constraintDirective = function($stateParams, DosageService, InterventionService, ScaledUnitResource) {
    return {
      scope: {
        model: '=',
        datasetUuid: '@'
      },
      link: function(scope) {
        scope.model = {};
        scope.LOWER_BOUND_OPTIONS = InterventionService.LOWER_BOUND_OPTIONS;
        scope.UPPER_BOUND_OPTIONS = InterventionService.UPPER_BOUND_OPTIONS;
        scope.toggleLowerBound = toggleLowerBound;
        scope.toggleUpperBound = toggleUpperBound;

        function toggleLowerBound(newState) {
          if (!newState) {
            delete scope.model.lowerBound;
          } else {
            scope.model.lowerBound = {
              type: InterventionService.LOWER_BOUND_OPTIONS[0],
              unit: scope.units[0]
            };
          }
        }

        function toggleUpperBound(newState) {
          if (!newState) {
            delete scope.model.upperBound;
          } else {
            scope.model.upperBound = {
              type: InterventionService.UPPER_BOUND_OPTIONS[0],
              unit: scope.units[0]
            };
          }
        }

        ScaledUnitResource.get($stateParams).$promise.then(function(scaledUnits){
          scope.scaledUnits = scaledUnits;
        });

        DosageService.get($stateParams.userUid, scope.datasetUuid).then(function(units) {
          scope.units = units;
        });
      },
      restrict: 'E',
      templateUrl: '/app/js/intervention/constraintDirective.html'
    };
  };

  return dependencies.concat(constraintDirective);
});
