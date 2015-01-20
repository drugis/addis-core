'use strict';
define([], function() {
  var dependencies = ['DurationService'];

  var DurationInputDirective = function(DurationService) {
    return {
      restrict: 'E',
      templateUrl: 'app/js/util/directives/durationInput/durationInput.html',
      scope: {
        durationString: '='
      },
      link: function(scope) {
        scope.parseDuration = DurationService.parseDuration;
        scope.generateDurationString = DurationService.generateDurationString;
        scope.periodTypeOptions = DurationService.getPeriodTypeOptions();
        if (typeof scope.durationString !== 'string') {
          scope.durationString = 'PT1H';
        }
        scope.durationCache = scope.parseDuration(scope.durationString);
      }
    };
  };
  return dependencies.concat(DurationInputDirective);
});
