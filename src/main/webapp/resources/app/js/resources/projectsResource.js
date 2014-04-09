'use strict';
define(['angular', 'angular-resource'], function (angular, angularResource) {
  var dependencies = ['$resource'];
  var ProjectsResource = function ($resource) {
    return $resource('/projects/:projectId', {projectId: '@projectId'});
  };
  return dependencies.concat(ProjectsResource);
});
