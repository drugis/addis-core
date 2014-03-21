'use strict';
define([], function () {
  var dependencies = ['$resource'];
  var ProblemService = function ($resource) {
    return $resource('/projects/:projectId/analyses/:analysisId/problem', {projectId: '@projectId', analysisId: '@id'});
  };
  return dependencies.concat(ProblemService);
});
