'use strict';
define(['angular', 'angular-resource'], function (angular, angularResource) {
  var dependencies = ['$resource'];
  var InterventionService = function ($resource) {
    return $resource('/projects/:projectId/intervention/:interventionId', {projectId: '@projectId', interventionId: '@id'});
  };
  return dependencies.concat(InterventionService);
});
