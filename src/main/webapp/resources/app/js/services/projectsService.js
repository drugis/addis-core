'use strict';
define(['angular', 'angular-resource'], function (angular, angularResource) {
  var dependencies = ['$resource'];
  var ProjectsService = function ($resource) {
    return $resource('/projects/:projectId', {id: '@projectId'});
  };
  return dependencies.concat(ProjectsService);
});
