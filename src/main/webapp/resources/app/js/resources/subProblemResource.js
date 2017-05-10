'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var SubProblemResource = function($resource) {
    return $resource('/projects/:projectId/analyses/:analysisId/problems/:problemId', {
      projectId: '@projectId',
      analysisId: '@analysisId',
      problemId: '@problemId'
    });
  };
  return dependencies.concat(SubProblemResource);
});