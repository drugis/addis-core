'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var GraphResource = function($resource) {
    return $resource(
      '/datasets/:datasetUUID/graphs/:graphUuid', {
        datasetUUID: '@datasetUUID',
        graphUuid: '@graphUuid',
        commitTitle: '@commitTitle',
        commitDescription: '@commitDescription'
      }, {
        'get': {
          method: 'get',
          headers: {
            'Content-Type': 'text/n3'
          },
          transformResponse: function(data) {
            return {
              data: data // property on Responce object to access raw result data
            };
          }
        },
        'put': {
          method: 'put'
        }
      });
  };
  return dependencies.concat(GraphResource);
});
