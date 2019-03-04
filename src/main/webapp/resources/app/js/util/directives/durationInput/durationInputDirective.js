'use strict';
define([], function() {
  var dependencies = ['DurationService'];

  var DurationInputDirective = function(DurationService) {
    /* jslint unused: true  */
    return {
      restrict: 'E',
      templateUrl: function(element, attr) {
        return attr.templateUrl ? attr.templateUrl : 'durationInputDirective.html';
      },
      scope: {
        durationString: '='
      },
      link: function(scope) {
        scope.parseDuration = DurationService.parseDuration;
        scope.generateDurationString = DurationService.generateDurationString;
        scope.periodTypeOptions = DurationService.getPeriodTypeOptions();
        scope.$watch('durationString', function(){
          scope.durationScratch = scope.parseDuration(scope.durationString);
        });
        if (typeof scope.durationString !== 'string') {
          scope.durationString = 'P1W';
        }
        scope.durationScratch = scope.parseDuration(scope.durationString);
      }
    };
  };
  return dependencies.concat(DurationInputDirective);
});
