'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var ScaledUnitResource = function($resource) {
    return $resource('/projects/:projectId/scaledUnits', {
      projectId: '@projectId'
    });
  };
  return dependencies.concat(ScaledUnitResource);
});
