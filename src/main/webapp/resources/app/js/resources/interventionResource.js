'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var InterventionResource = function($resource) {
    return $resource('/projects/:projectId/interventions/:interventionId', {
      projectId: '@projectId',
      interventionId: '@id'
    }, {
      'delete': {
        method: 'DELETE'
      },
      'queryByProject': {
        url: '/projects/:projectId/interventions',
        method: 'GET',
        isArray: true
      }
    });
  };
  return dependencies.concat(InterventionResource);
});
