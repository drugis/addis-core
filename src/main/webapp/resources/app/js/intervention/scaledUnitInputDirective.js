'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$stateParams', 'MappingService', 'ScaledUnitResource'];
  var scaledUnitInputDirective = function($stateParams, MappingService, ScaledUnitResource) {
    return {
      scope: {
        unitConcepts: '=',
        scaledUnits: '=',
        noCancel: '='
      },
      link: function(scope) {
        scope.metricMultipliers = MappingService.METRIC_MULTIPLIERS;
        scope.newScaledUnit = {
          multiplier: scope.metricMultipliers[5],
          unit: scope.unitConcepts[0]
        };
        setDefaultName(scope.newScaledUnit);
        scope.hasCancel = scope.noCancel ? false : true;

        //functions
        scope.saveScaledUnit = saveScaledUnit;
        scope.isDuplicateName = isDuplicateName;
        scope.setDefaultName = setDefaultName;
        scope.cancelScaledUnit = cancelScaledUnit;

        function saveScaledUnit(newScaledUnit) {
          var saveCommand = {
            conceptUri: newScaledUnit.unit.unitUri,
            multiplier: newScaledUnit.multiplier.conversionMultiplier,
            name: newScaledUnit.name
          };
          scope.isSavingUnit = true;
          ScaledUnitResource.save($stateParams, saveCommand).$promise.then(function() {
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

        function cancelScaledUnit(){
          scope.$emit('scaledUnitCancelled');
        }
      },
      restrict: 'E',
      templateUrl: './scaledUnitInputDirective.html'
    };
  };

  return dependencies.concat(scaledUnitInputDirective);
});
