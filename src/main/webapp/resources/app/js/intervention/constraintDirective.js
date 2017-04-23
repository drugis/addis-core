'use strict';
define([], function() {
  var dependencies = ['$stateParams', 'ScaledUnitResource', 'DosageService'];
  var constraintDirective = function($stateParams, ScaledUnitResource, DosageService) {
    return {
      scope: {
        model: '=',
        datasetUuid: '@'
      },
      restrict: 'E',
      templateUrl: '/app/js/intervention/constraintDirective.html',
      link: function(scope) {
        // vars
        scope.model = {};

        //loading
        DosageService.get($stateParams.userUid, scope.datasetUuid).then(function(units) {
          scope.unitConcepts = units;
        });
        scope.scaledUnits = ScaledUnitResource.query($stateParams);
        scope.$on('scaledUnitsChanged', function() {
          scope.scaledUnits = ScaledUnitResource.query($stateParams);
        });
      },
    };
  };

  return dependencies.concat(constraintDirective);
});
