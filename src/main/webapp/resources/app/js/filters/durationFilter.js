'use strict';
define(['moment'], function(moment) {
  var dependencies = [];
  var DurationFilter = function() {
    return function(duration) {
      return duration ? moment.duration(duration).humanize() : undefined;
    };
  };
  return dependencies.concat(DurationFilter);
});