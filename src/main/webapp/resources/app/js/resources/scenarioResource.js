'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var ScenarioResource = function($resource) {
    return $resource('/projects/:projectId/analyses/:analysisId/scenarios/:id', {
      projectId: '@projectId',
      analysisId: '@analysisId',
      id: '@id'
    });
  };
  return dependencies.concat(ScenarioResource);
});