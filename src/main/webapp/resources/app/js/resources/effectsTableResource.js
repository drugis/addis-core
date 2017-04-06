'use strict';
define(function() {
  var dependencies = ['$resource'];
  var EffectsTableResource = function($resource) {
    return $resource('/projects/:projectId/analyses/:analysisId/effectsTable', {
      projectId: '@projectId',
      analysisId: '@analysisId'
    }, {
      'setEffectsTableExclusion': {
        url: '/projects/:projectId/analyses/:analysisId/effectsTable',
        method: 'POST'
      }, 'getEffectsTableExclusions': {
        url: '/projects/:projectId/analyses/:analysisId/effectsTable',
        method: 'GET',
        isArray: true
      }
    });
  };
  return dependencies.concat(EffectsTableResource);
});