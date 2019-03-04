'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var WorkspaceSettingsResource = function($resource) {
    return $resource('/projects/:projectId/analyses/:analysisId/workspaceSettings', {
      projectId: '@projectId',
      analysisId: '@analysisId'
    }, {
        put: {
          method: 'PUT'
        }
      });
  };
  return dependencies.concat(WorkspaceSettingsResource);
});
