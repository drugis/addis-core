'use strict';
define(['angular'], function() {
  var dependencies = ['$stateParams', 'UnitNamesService'];
  var constraintDirective = function($stateParams, UnitNamesService) {
    var LOWER_BOUND_OPTIONS = [{
        value: 'atLeast',
        label: 'At least (>=)'
      }, {
        value: 'moreThan',
        label: 'More than (>)'
      }, {
        value: 'exactly',
        label: 'Exactly (=)'
      }],
      UPPER_BOUND_OPTIONS = [{
        value: 'lessThan',
        label: 'Less than (<)'
      }, {
        value: 'atMost',
        label: 'At most (<=)'
      }];

    function switchBound() {}
    return {
      scope: {
        model: '=',
        datasetUuid: '@'
      },
      link: function(scope) {
        scope.LOWER_BOUND_OPTIONS = LOWER_BOUND_OPTIONS;
        scope.UPPER_BOUND_OPTIONS = UPPER_BOUND_OPTIONS;
        scope.switchBound = switchBound;

        UnitNamesService.get($stateParams.userUid, scope.datasetUuid).then(function(units) {
          scope.units = units;
        });
      },
      restrict: 'E',
      templateUrl: '/app/js/intervention/constraintDirective.html'
    };
  };

  return dependencies.concat(constraintDirective);
});
