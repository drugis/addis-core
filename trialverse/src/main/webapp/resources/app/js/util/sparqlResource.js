'use strict';
define([], function() {
  var dependencies = ['$http'];
  var SparqlResource = function($http) {

    function get(name) {
      return $http.get('app/sparql/' + name, {
        name: name
      }).then(function(result) {
        return result.data;
      });
    }

    return {
      get: get
    };
  };
  return dependencies.concat(SparqlResource);
});