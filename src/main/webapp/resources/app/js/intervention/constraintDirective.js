'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$stateParams',
    'DosageService',
    'InterventionService',
    'ScaledUnitResource',
    'MappingService',
    'DurationService'
  ];
  var constraintDirective = function($stateParams,
    DosageService,
    InterventionService,
    ScaledUnitResource,
    MappingService,
    DurationService) {
    return {
      scope: {
        model: '=',
        datasetUuid: '@'
      },
      link: function(scope) {
        // vars
        scope.model = {};
        scope.LOWER_BOUND_OPTIONS = InterventionService.LOWER_BOUND_OPTIONS;
        scope.UPPER_BOUND_OPTIONS = InterventionService.UPPER_BOUND_OPTIONS;
        scope.metricMultipliers = MappingService.METRIC_MULTIPLIERS;
        scope.periodOptions = DurationService.getPeriodTypeOptions();
        scope.shows = {
          newLowerBoundScaledUnit: false,
          newUpperBoundScaledUnit: false,
          newLowerBoundPeriodicity: false,
          newUpperBoundPeriodicity: false
        };
        scope.newLowerBoundScaledUnit = {
          bound: 'lower'
        };
        scope.newUpperBoundScaledUnit = {
          bound: 'upper'
        };
        scope.lowerBoundPeriodicity = {};
        scope.upperBoundPeriodicity = {};

        //functions
        scope.toggleLowerBound = toggleLowerBound;
        scope.toggleUpperBound = toggleUpperBound;
        scope.toggleNewLowerBoundScaledUnit = toggleNewLowerBoundScaledUnit;
        scope.toggleNewUpperBoundScaledUnit = toggleNewUpperBoundScaledUnit;
        scope.toggleNewLowerBoundPeriodicity = toggleNewLowerBoundPeriodicity;
        scope.toggleNewUpperBoundPeriodicity = toggleNewUpperBoundPeriodicity;
        scope.saveScaledUnit = saveScaledUnit;
        scope.updateNewScaledUnit = updateNewScaledUnit;
        scope.updateUpperBoundPeriodicity = updateUpperBoundPeriodicity;
        scope.updateLowerBoundPeriodicity = updateLowerBoundPeriodicity;
        scope.duplicateName = duplicateName;
        scope.lowerBoundPeriod = lowerBoundPeriod;
        scope.upperBoundPeriod = upperBoundPeriod;

        //loading
        DosageService.get($stateParams.userUid, scope.datasetUuid).then(function(units) {
          scope.units = units;
        });
        loadScaledUnits();

        function toggleLowerBound(newState) {
          if (!newState) {
            delete scope.model.lowerBound;
            scope.shows.newLowerBoundScaledUnit = false;
            scope.shows.newLowerBoundPeriodicity = false;
          } else {
            scope.model.lowerBound = {
              type: InterventionService.LOWER_BOUND_OPTIONS[0],
              unit: scope.scaledUnits[0]
            };
            if (!scope.model.lowerBound.unit) {
              scope.model.lowerBound.unit = {
                unitPeriod: 'P1D'
              };
            } else {
              scope.model.lowerBound.unit.unitPeriod = 'P1D';
            }
          }
        }

        function toggleUpperBound(newState) {
          if (!newState) {
            delete scope.model.upperBound;
            scope.shows.newUpperBoundScaledUnit = false;
            scope.shows.newUpperBoundPeriodicity = false;
          } else {
            scope.model.upperBound = {
              type: InterventionService.UPPER_BOUND_OPTIONS[0],
              unit: scope.scaledUnits[0]
            };
            if (!scope.model.upperBound.unit) {
              scope.model.upperBound.unit = {
                unitPeriod: 'P1D'
              };
            } else {
              scope.model.upperBound.unit.unitPeriod = 'P1D';
            }
          }
        }

        function toggleNewLowerBoundScaledUnit() {
          if (scope.lowerBoundEnabled) {
            scope.shows.newLowerBoundScaledUnit = !scope.shows.newLowerBoundScaledUnit;
          }

        }

        function toggleNewUpperBoundScaledUnit() {
          if (scope.upperBoundEnabled) {
            scope.shows.newUpperBoundScaledUnit = !scope.shows.newUpperBoundScaledUnit;
          }
        }

        function toggleNewLowerBoundPeriodicity() {
          if (scope.lowerBoundEnabled) {
            scope.shows.newLowerBoundPeriodicity = !scope.shows.newLowerBoundPeriodicity;
          }
        }

        function toggleNewUpperBoundPeriodicity() {
          if (scope.upperBoundEnabled) {
            scope.shows.newUpperBoundPeriodicity = !scope.shows.newUpperBoundPeriodicity;
          }
        }

        function saveScaledUnit(scaledUnit) {
          var newScaledUnit = {
            projectId: $stateParams.projectId,
            conceptUri: scaledUnit.unit.unitUri,
            multiplier: scaledUnit.multiplier.conversionMultiplier,
            name: scaledUnit.name
          };
          if (scaledUnit.bound === 'lower') {
            toggleNewLowerBoundScaledUnit();
            scope.model.lowerBound.unit = newScaledUnit;
          }
          if (scaledUnit.bound === 'upper') {
            toggleNewUpperBoundScaledUnit();
            scope.model.upperBound.unit = newScaledUnit;
          }
          ScaledUnitResource.create($stateParams, newScaledUnit).$promise.then(function() {
            loadScaledUnits();
          });
        }

        function loadScaledUnits() {
          ScaledUnitResource.get($stateParams).$promise.then(function(scaledUnits) {
            scope.scaledUnits = scaledUnits;
          });
        }

        function updateNewScaledUnit(newScaledUnit) {
          if (newScaledUnit.multiplier.conversionMultiplier === 1e00) {
            newScaledUnit.name = newScaledUnit.unit.unitName;
          } else {
            newScaledUnit.name = newScaledUnit.multiplier.label + newScaledUnit.unit.unitName;
          }
        }

        function duplicateName(name) {
          return _.find(scope.scaledUnits, ['name', name]);
        }

        function updateLowerBoundPeriodicity() {
          scope.shows.newLowerBoundPeriodicity = false;
          var duration = {
            periodType: scope.lowerBoundPeriodicity.unit,
            numberOfPeriods: scope.lowerBoundPeriodicity.amount
          };
          scope.model.lowerBound.unit.unitPeriod = DurationService.generateDurationString(duration);
          if (!scope.model.lowerBound.unit.unitPeriod) {
            scope.model.lowerBound.unit.unitPeriod = 'P1D';
          }
        }

        function updateUpperBoundPeriodicity() {
          scope.shows.newUpperBoundPeriodicity = false;
          var duration = {
            periodType: scope.upperBoundPeriodicity.unit,
            numberOfPeriods: scope.upperBoundPeriodicity.amount
          };
          scope.model.upperBound.unit.unitPeriod = DurationService.generateDurationString(duration);
          if (!scope.model.upperBound.unit.unitPeriod) {
            scope.model.upperBound.unit.unitPeriod = 'P1D';
          }
        }

        function upperBoundPeriod() {
          if (!scope.upperBoundPeriodicity) {
            scope.model.upperBound.unit.unitPeriod = 'P1D';
          } else {
            updateUpperBoundPeriodicity();
          }
        }

        function lowerBoundPeriod() {
          if (!scope.model.lowerBound.unit.unitPeriod) {
            scope.model.lowerBound.unit.unitPeriod = 'P1D';
          } else {
            updateLowerBoundPeriodicity();
          }
        }
      },
      restrict: 'E',
      templateUrl: '/app/js/intervention/constraintDirective.html'
    };
  };

  return dependencies.concat(constraintDirective);
});
