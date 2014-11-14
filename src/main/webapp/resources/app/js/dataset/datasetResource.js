'use strict';
define([], function(rdfstore) {

  var dependencies = ['$resource', '$q'];
  var DatasetResource = function($resource, $q) {
    return $resource('/datasets/:datasetUUID', {
      datasetUUID: '@datasetUUID'
    }, {
      'query': {
        method: 'GET',
        isArray: false,
        transformResponse: function(data, headersGetter){
          return {graphData: data}
        },
      }
    });
  };
  return dependencies.concat(DatasetResource);
});
