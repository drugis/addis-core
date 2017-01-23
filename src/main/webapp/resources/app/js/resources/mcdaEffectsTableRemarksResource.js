'use strict';
define([], function() {
  var dependencies = ['$resource'];

  var RemarksResource = function($resource) {
    return $resource(window.config.workspacesRepositoryUrl + '/remarks', {
      analysisId: '@workspaceId'
    });
  };
  return dependencies.concat(RemarksResource);
});