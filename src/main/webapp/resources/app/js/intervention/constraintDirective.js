'use strict';
define([], function() {
  var dependencies = [];
  var constraintDirective = function() {
    return {
      scope: {
        model: '=',
        scaledUnits: '='
      },
      restrict: 'E',
      templateUrl: './constraintDirective.html'
    };
  };

  return dependencies.concat(constraintDirective);
});
