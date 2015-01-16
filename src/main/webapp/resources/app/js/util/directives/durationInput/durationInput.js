'use strict';
define([], function() {
  var dependencies = [];

  var DurationInputDirective = function() {
    return {
      restrict: 'E',
      templateUrl: 'app/js/navbar/durationInputDirective.html',
      scope: {
        duration: '='
      },
      link: function(scope) {
      }
    };
  };
  return dependencies.concat(DurationInputDirective);
});
