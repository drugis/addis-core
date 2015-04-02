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
        'put': {
          method: 'put'
        }
      });
  };
  return dependencies.concat(GraphResource);
});