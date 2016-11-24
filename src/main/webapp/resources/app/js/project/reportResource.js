'use strict';
define(['angular-resource'], function () {
  var dependencies = ['$resource'];
  var ReportResource = function ($resource) {
    return $resource('/projects/:projectId/report', {projectId: '@projectId'}, {
      get: {
        method: 'GET',
        transformResponse: function(data) {
          return {
            data: data
          };
        }
      },
      put: {
        method: 'PUT'
      },
      delete:{
        method: 'DELETE',
        transformResponse: function(data){
          return {
            data:data
          };
        }
      }
    });
  };
  return dependencies.concat(ReportResource);
});
