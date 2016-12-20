'use strict';
define(['angular-resource'], function () {
  var dependencies = ['$resource'];
  var ProjectResource = function ($resource) {
    return $resource('/projects/:projectId', {projectId: '@projectId'}, {
      setArchived : {
        url: '/projects/:projectId/setArchivedStatus',
        method: 'POST'
      }, copy : {
        url: '/projects/:projectId/copy',
        method: 'POST', transformResponse: function(data) {
          return {
            newProjectId: Number.parseInt(data)
          };
        }
      }
    });
  };
  return dependencies.concat(ProjectResource);
});
