'use strict';
define([], function() {
  var dependencies = ['DurationService'];

  var DurationInputDirective = function(DurationService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/navbar/durationInputDirective.html',
      scope: {
        durationString: '='
      },
      link: function(scope) {
        scope.parseDuration = DurationService.parseDuration;
        scope.generateDurationString = DurationService.generateDurationString;
        scope.periodTypeOptions = DurationService.getPeriodTypeOptions();
        scope.durationCache = scope.parseDuration(durationString);
      }
    };
  };
  return dependencies.concat(DurationInputDirective);
});
