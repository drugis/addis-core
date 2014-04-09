'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var ScenarioResource = function($resource) {
    return $resource('/projects/:projectId/analyses/:analysisId/scenarios/:scenarioId', {
      projectId: '@projectId',
      analysisId: '@analysisId',
      scenarioId: '@id'
    });
  };
  return dependencies.concat(ScenarioResource);
});