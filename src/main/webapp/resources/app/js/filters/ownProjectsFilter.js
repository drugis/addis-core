'use strict';
define(['lodash', 'angular'], function(_) {
  var dependencies = [];
  var OwnProjectsFilter = function() {
    return function(projects, userId, negate) {
      return _.filter(projects, function(project) {
        return negate ? project.owner.id !== userId : project.owner.id === userId;
      });
    };
  };
  return dependencies.concat(OwnProjectsFilter);
});
