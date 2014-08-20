'use strict';
define([], function() {
  var dependencies = [];
  var ActivityTypeFilter = function() {
    return function(activityTypeUri) {
      if(activityTypeUri) {
        return activityTypeUri.substring(activityTypeUri.lastIndexOf('#') + 1, activityTypeUri.length - ('Activity'.length));
      }

      return undefined;
    };
  };
  return dependencies.concat(ActivityTypeFilter);
});
                                    