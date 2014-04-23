'use strict';
define(['angular', 'angular-resource'], function (angular, angularResource) {
  var dependencies = ['$resource'];
  var ProjectResource = function ($resource) {
    return $resource('/projects/:projectId', {projectId: '@projectId'});
  };
  return dependencies.concat(ProjectResource);
});
