'use strict';
define(['angular'], function(angular) {
  var dependencies = [];
  var OwnProjectsFilter = function() {
    return function(projects, userId, negate) {
      var filtered = [];
      if (projects.$resolved) {
        angular.forEach(projects, function(project) {
          if (negate ? project.owner.id !== userId : project.owner.id === userId) {
            this.push(project);
          }
        }, filtered);
      }
      return filtered;
    };
  };
  return dependencies.concat(OwnProjectsFilter);
});
