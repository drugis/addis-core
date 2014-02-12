'use strict';
define([], function() {
  var dependencies = [];
    var OwnProjectFilter = function () {
      return function(project, userId) {
        if(project.owner.id === userId){
          return project;
        }
      }
    };
return dependencies.concat(OwnProjectFilter);
});