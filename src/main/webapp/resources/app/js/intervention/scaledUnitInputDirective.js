'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$stateParams', 'MappingService', 'ScaledUnitResource'];
  var scaledUnitInputDirective = function($stateParams, MappingService, ScaledUnitResource) {
    return {
      scope: {
        unitConcepts: '=',
        scaledUnits: '='
      },
      link: function(scope) {
        scope.metricMultipliers = MappingService.METRIC_MULTIPLIERS;
        scope.newScaledUnit = {
          multiplier: scope.metricMultipliers[5],
          unit: scope.unitConcepts[0]
        };
        setDefaultName(scope.newScaledUnit);

        //functions
        scope.saveScaledUnit = saveScaledUnit;
        scope.isDuplicateName = isDuplicateName;
        scope.setDefaultName = setDefaultName;

        function saveScaledUnit(newScaledUnit) {
          var saveCommand = {
            conceptUri: newScaledUnit.unit.unitUri,
            multiplier: newScaledUnit.multiplier.conversionMultiplier,
            name: newScaledUnit.name
          };
          scope.isSavingUnit = true;
          ScaledUnitResource.save($stateParams, saveCommand).$promise.then(function() {
            delete scope.isAddingScaledUnit;
            scope.isSavingUnit = false;
            scope.$emit('scaledUnitsChanged');
          });
        }

        function setDefaultName(newScaledUnit) {
          if (newScaledUnit.multiplier.conversionMultiplier === 1e00) {
            newScaledUnit.name = newScaledUnit.unit.unitName;
          } else {
            newScaledUnit.name = newScaledUnit.multiplier.label + newScaledUnit.unit.unitName;
          }
        }

        function isDuplicateName(name) {
          return _.find(scope.scaledUnits, ['name', name]);
        }


      },
      restrict: 'E',
      templateUrl: '/app/js/intervention/scaledUnitInputDirective.html'
    };
  };

  return dependencies.concat(scaledUnitInputDirective);
});
