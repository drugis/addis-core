'use strict';
define(['angular'], function() {
  var dependencies = ['$stateParams', 'DosageService'];
  var constraintDirective = function($stateParams, DosageService) {
    var LOWER_BOUND_OPTIONS = [{
        value: 'AT_LEAST',
        label: 'At least (>=)'
      }, {
        value: 'MORE_THAN',
        label: 'More than (>)'
      }, {
        value: 'EXACTLY',
        label: 'Exactly (=)'
      }],
      UPPER_BOUND_OPTIONS = [{
        value: 'LESS_THAN',
        label: 'Less than (<)'
      }, {
        value: 'AT_MOST',
        label: 'At most (<=)'
      }];

    return {
      scope: {
        model: '=',
        datasetUuid: '@'
      },
      link: function(scope) {
        scope.model = {};
        scope.LOWER_BOUND_OPTIONS = LOWER_BOUND_OPTIONS;
        scope.UPPER_BOUND_OPTIONS = UPPER_BOUND_OPTIONS;
        scope.toggleLowerBound = toggleLowerBound;
        scope.toggleUpperBound = toggleUpperBound;

        function toggleLowerBound(newState) {
          if (!newState) {
            delete scope.model.lowerBound;
          } else {
            scope.model.lowerBound = {
              type: LOWER_BOUND_OPTIONS[0],
              unit: scope.units[0]
            };
          }
        }

        function toggleUpperBound(newState) {
          if (!newState) {
            delete scope.model.upperBound;
          } else {
            scope.model.upperBound = {
              type: UPPER_BOUND_OPTIONS[0],
              unit: scope.units[0]
            };
          }
        }

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
