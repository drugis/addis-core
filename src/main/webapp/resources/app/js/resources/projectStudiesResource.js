'use strict';
define([], function () {
  var dependencies = ['$resource'];
  var ProjectStudiesResource = function ($resource) {
    return $resource('/projects/:projectId/studies');
  };
  return dependencies.concat(ProjectStudiesResource);
});
