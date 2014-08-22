'use strict';
define([], function() {
  var dependencies = ['$filter'];
  var DurationOffsetFilter = function($filter) {
    return function(duration) {
      var durationLabel = ($filter('durationFilter')(duration));
      var offsetLabel = (duration.indexOf('-') === 0 ? ' before ' : ' after ');
      return durationLabel ? durationLabel + offsetLabel : '';
    };
  };
  return dependencies.concat(DurationOffsetFilter);
});