'use strict';
define(['angular-resource'], function () {
  var dependencies = ['$resource'];
  var ProjectResource = function ($resource) {
    return $resource('/projects/:projectId', {projectId: '@projectId'}, {
      setArchived : {
        url: '/projects/:projectId/setArchivedStatus',
        method: 'POST'
      }
    });
  };
  return dependencies.concat(ProjectResource);
});
