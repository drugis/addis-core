'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var ScenarioResource = function($resource) {
    return $resource('/projects/:projectId/analyses/:analysisId/problems/:problemId/scenarios/:id', {
      projectId: '@projectId',
      analysisId: '@analysisId',
      problemId: '@problemId',
      id: '@id'
    });
  };
  return dependencies.concat(ScenarioResource);
});