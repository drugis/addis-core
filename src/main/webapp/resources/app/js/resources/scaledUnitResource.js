'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var ScaledUnitResource = function($resource) {
    return $resource('/projects/:projectId/scaledUnits', {
      projectId: '@projectId'
    }, {
      'create': {
        method: 'POST'
      },
      'get': {
        method: 'GET',
        isArray: true
      }
    });
  };
  return dependencies.concat(ScaledUnitResource);
});
