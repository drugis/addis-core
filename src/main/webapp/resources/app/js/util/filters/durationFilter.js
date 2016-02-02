'use strict';
define(['moment'], function(moment) {
  var dependencies = [];
  var DurationFilter = function() {
    return function(duration) {
      if (!duration) {
        return duration;
      } else if (duration === 'P0D' || duration === '-P0D') {
        return null;
      } else if (duration === 'PT0S') {
        return 'instantaneous';
      } else {
        var momentDuration = moment.duration(duration);
        if (momentDuration.asDays() >= 1) {
          return momentDuration.asDays() + ' day(s)';
        } else {
          return momentDuration.asHours() + ' hour(s)';
        }
      }
    };
  };
  return dependencies.concat(DurationFilter);
});
