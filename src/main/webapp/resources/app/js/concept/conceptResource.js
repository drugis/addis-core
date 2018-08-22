'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var ConceptResource = function($resource) {
    return $resource('/datasets/:datasetUuid/concepts', {
      datasetUuid: '@datasetUuid'
    }, {
      'get': {
        method: 'get',
        headers: {
          'Accept': 'text/turtle',
          'Content-Type': 'text/turtle'
        },
        transformResponse: function(data) {
          return {
            data: data // property on Response object to access raw result data
          };
        }
      },
      'query': {
        method: 'GET',
        headers: {
          'Accept': 'text/turtle'
        },
        isArray: false,
        transformResponse: function(data) {
          return {
            data: data // property on Response object to access raw result data
          };
        },
      }
    });
  };
  return dependencies.concat(ConceptResource);
});
