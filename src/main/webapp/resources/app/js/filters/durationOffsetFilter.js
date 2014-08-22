'use strict';
define(['moment'], function(moment) {
  var dependencies = [];
  var DurationOffsetFilter = function() {
    return function(duration) {
      if (!duration) {
        return duration;
      } else if (duration === "P0D" || duration === "-P0D") {
        return null;
      } else {
        var durationLabel = moment.duration(duration).humanize();
        return durationLabel + (duration.indexOf('-') === 0 ? ' before ' : ' after ');
      }
    };
  };
  return dependencies.concat(DurationOffsetFilter);
});