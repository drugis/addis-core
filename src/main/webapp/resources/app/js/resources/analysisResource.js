'use strict';
define([], function() {
  var dependencies = ['$resource', 'ValueTreeService'];
  var AnalysisResource = function($resource, ValueTreeService) {
    return $resource('/projects/:projectId/analyses/:analysisId', {
      projectId: '@projectId',
      analysisId: '@id'
    }, {
      get: {
        method: 'GET',
        interceptor: {
          response: function(response) {
            if(response.data.problem) {
              return ValueTreeService.addDerived(response)
            }
            return response.resource;
          }
        }
      },
      save: {
        method: 'POST',
        interceptor: {
          response: function(response) {
            if(response.data.problem) {
              return ValueTreeService.addDerived(response)
            }
            return response.resource;
          }
        }
      }
    }
    );
  };
  return dependencies.concat(AnalysisResource);
});
