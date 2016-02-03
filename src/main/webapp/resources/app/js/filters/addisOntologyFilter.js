'use strict';
define([], function() {
  var dependencies = [];
  var AddisOntologyFilter = function() {
    return function(activityTypeUri) {
      if (activityTypeUri) {
        return activityTypeUri.substring(activityTypeUri.lastIndexOf('#') + 1, activityTypeUri.length);
      }
      return activityTypeUri;
    };
  };
  return dependencies.concat(AddisOntologyFilter);
});
