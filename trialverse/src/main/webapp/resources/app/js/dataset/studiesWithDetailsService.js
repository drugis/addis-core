'use strict';
define([], function() {
  var dependencies = ['$http', 'SparqlResource', 'RemoteRdfStoreService'];
  var StudiesWithDetailsService = function($http, SparqlResource, RemoteRdfStoreService) {

    var queryStudiesWithDetails = SparqlResource.get('queryStudiesWithDetails.sparql');

    function get(userUid, datasetUuid, datasetVersionUuid) {
      return queryStudiesWithDetails.then(function(query) {
        return $http.get(
          '/users/'+ userUid +'/datasets/' + datasetUuid + '/versions/' + datasetVersionUuid + '/query', {
            params: {
              query: query
            },
            headers: {
              Accept: 'application/sparql-results+json'
            },
            transformResponse: function(data) {
              return RemoteRdfStoreService.deFusekify(data);
            }
          });
      }).then(function(response) {
        return response.data;
      });
    }

    return {
      get: get
    };
  };
  return dependencies.concat(StudiesWithDetailsService);
});