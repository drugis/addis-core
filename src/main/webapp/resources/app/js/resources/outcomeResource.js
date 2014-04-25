'use strict';
define(['angular', 'angular-resource'], function (angular, angularResource) {
  var dependencies = ['$resource'];
  var OutcomeResource = function ($resource) {
    return $resource('/projects/:projectId/outcomes/:outcomeId', {projectId: '@projectId', outcomeId: '@id'});
  };
  return dependencies.concat(OutcomeResource);
});
