'use strict';
define([], function() {

  var dependencies = ['$resource'];

  var SparqlResource = function($resource) {

    return $resource('app/sparql/:name', {
      name: name
    }, {
      'get': {
        method: 'get',
        transformResponse: function(data) {
          return {
            data: data
          };
        }
      }
    });
  };
  return dependencies.concat(SparqlResource);
});
