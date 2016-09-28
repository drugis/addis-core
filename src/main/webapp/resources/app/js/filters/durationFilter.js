'use strict';
define(['moment'], function(moment) {
  var dependencies = [];
  var DurationFilter = function() {
    return function(duration) {
      if (!duration) {
        return duration;
      } else if (duration === 'P0D' || duration === '-P0D') {
        return null;
      } else if(duration === 'PT0S') {
        return 'instantaneous';
      } else {
        return moment.duration(duration).humanize();
      }
    };
  };
  return dependencies.concat(DurationFilter);
});
