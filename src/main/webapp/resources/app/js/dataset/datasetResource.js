'use strict';
define([], function() {

  var dependencies = ['$resource'];
  var DatasetResource = function($resource) {
    return $resource('/datasets', {}, {
      'query': {
        method: 'GET',
        headers: {
          'Accept': 'text/turtle'
        },
        isArray: false,
        transformResponse: function(data) {
          return {
            data: data // property on Responce object to access raw result data
          };
        },
      }
    });
  };
  return dependencies.concat(DatasetResource);
});