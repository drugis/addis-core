'use strict';
define(['angular-resource'], function (angularResource) {
  var dependencies = ['$resource'];
  var ProjectResource = function ($resource) {
    return $resource('/projects/:projectId', {projectId: '@projectId'});
  };
  return dependencies.concat(ProjectResource);
});
