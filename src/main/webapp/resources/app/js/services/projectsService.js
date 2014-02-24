'use strict';
define(['angular', 'angular-resource'], function (angular, angularResource) {
  var dependencies = ['$resource'];
  var ProjectsService = function ($resource) {
    return $resource('/projects/:id', {id: '@id'});
  };
  return dependencies.concat(ProjectsService);
});
