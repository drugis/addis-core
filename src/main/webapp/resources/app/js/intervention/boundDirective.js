'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$stateParams', 'InterventionService', 'MappingService', 'ScaledUnitResource'];
  var boundDirective = function($stateParams, InterventionService, MappingService, ScaledUnitResource) {
    return {
      scope: {
        boundType: '=',
        bound: '=',
        scaledUnits: '=',
        unitConcepts: '='
      },
      link: function(scope) {
        // vars
        scope.model = {};
        if (scope.boundType === 'lower') {
          scope.boundName = 'Lower';
          scope.BOUND_OPTIONS = InterventionService.LOWER_BOUND_OPTIONS;
        } else {
          scope.boundName = 'Upper';
          scope.BOUND_OPTIONS = InterventionService.UPPER_BOUND_OPTIONS;
        }
        scope.metricMultipliers = MappingService.METRIC_MULTIPLIERS;

        //functions
        scope.toggleBound = toggleBound;
        scope.openScaledUnitCreation = openScaledUnitCreation;
        scope.saveScaledUnit = saveScaledUnit;
        scope.updatePeriodicity = updatePeriodicity;
        scope.isDuplicateName = isDuplicateName;
        scope.setDefaultName = setDefaultName;
        scope.unitPeriod = 'P1D';



        function toggleBound(newState) {
          if (!newState) {
            delete scope.bound;
          } else {
            scope.bound = {
              type: scope.BOUND_OPTIONS[0],
              unit: scope.scaledUnits[0]
            };
          }
        }

        function openScaledUnitCreation() {
          scope.scaledUnits.isAddingScaledUnit = scope.boundType;
          scope.newScaledUnit = {
            multiplier: scope.metricMultipliers[5],
            unit: scope.scaledUnits[0]
          };
          scope.setDefaultName(scope.newScaledUnit);
        }

        function saveScaledUnit(newScaledUnit) {
          var saveCommand = {
            conceptUri: newScaledUnit.unit.unitUri,
            multiplier: newScaledUnit.multiplier.conversionMultiplier,
            name: newScaledUnit.name
          };
          scope.isSavingUnit = true;
          ScaledUnitResource.save($stateParams, saveCommand).$promise.then(function() {
            scope.$emit('scaledUnitsChanged');
            scope.isSavingUnit = false;
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

        function updatePeriodicity() {
          var duration = {
            periodType: scope.lowerBoundPeriodicity.unit,
            numberOfPeriods: scope.lowerBoundPeriodicity.amount
          };
          scope.model.lowerBound.unit.unitPeriod = DurationService.generateDurationString(duration);
          if (!scope.model.lowerBound.unit.unitPeriod) {
            scope.model.lowerBound.unit.unitPeriod = 'P1D';
          }
        }

        function updatePeriod() {
          if (!scope.model.lowerBound.unit.unitPeriod) {
            scope.model.lowerBound.unit.unitPeriod = 'P1D';
          } else {
            updateLowerBoundPeriodicity();
          }
        }
      },
      restrict: 'E',
      templateUrl: '/app/js/intervention/boundDirective.html'
    };
  };

  return dependencies.concat(boundDirective);
});
