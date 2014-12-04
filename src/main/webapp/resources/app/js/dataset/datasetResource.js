'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var DatasetResource = function($resource) {
    return $resource('/datasets/:datasetUUID', {
      datasetUUID: '@datasetUUID'
    }, {
      'get': {
        method: 'get',
        headers: {
          'Accept': 'text/n3'
        },
        transformResponse: function(data) {
          return {
            n3Data: data // property on Responce object to access raw result data
          };
        }
      },
      'query': {
        method: 'GET',
        headers: {
          'Accept': 'text/n3'
        },
        isArray: false,
        transformResponse: function(data) {
          return {
            n3Data: data // property on Responce object to access raw result data
          };
        },
      }
    });
  };
  return dependencies.concat(DatasetResource);
});
