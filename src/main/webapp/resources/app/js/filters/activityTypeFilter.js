'use strict';
define([], function() {
  var dependencies = [];
  var endsWith = function endsWith(str, suffix) {
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
  };
  var ActivityTypeFilter = function() {
    return function(activityTypeUri) {
      if (activityTypeUri && endsWith(activityTypeUri, 'Activity')) {
        return activityTypeUri.substring(0, (activityTypeUri.length - ('Activity'.length)));
      }
      return activityTypeUri;
    };
  };
  return dependencies.concat(ActivityTypeFilter);
});