'use strict';
define(['angular-resource'], function () {
  var dependencies = ['$resource'];
  var ReportResource = function ($resource) {
    return $resource('/projects/:projectId/report', {projectId: '@projectId'}, {
      get: {
        method: 'GET',
        transformResponse: function(data) {
          console.log('getreport');
          return {
            data: data
          };
        }
      },
      put: {
        method: 'PUT'
      }
    });
  };
  return dependencies.concat(ReportResource);
});
