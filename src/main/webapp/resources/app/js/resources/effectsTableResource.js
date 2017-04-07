'use strict';
define(function() {
  var dependencies = ['$resource'];
  var EffectsTableResource = function($resource) {
    return $resource('/projects/:projectId/analyses/:analysisId/effectsTable', {
      projectId: '@projectId',
      analysisId: '@analysisId'
    }, {
      'toggleExclusion': {
        url: '/projects/:projectId/analyses/:analysisId/effectsTable',
        method: 'POST'
      }
    });
  };
  return dependencies.concat(EffectsTableResource);
});