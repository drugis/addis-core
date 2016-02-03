'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$http', 'SparqlResource'];
  var StudiesWithDetailsService = function($http, SparqlResource) {

    var queryStudiesWithDetails = SparqlResource.get('queryStudiesWithDetails.sparql');

    function deFusekify(data) {
     var json = JSON.parse(data);
     var bindings = json.results.bindings;
     return _.map(bindings, function(binding) {
       return _.fromPairs(_.map(_.toPairs(binding), function(obj) {
         return [obj[0], obj[1].value];
       }));
     });
   }

    function get(userUid, datasetUuid, datasetVersionUuid) {
      return queryStudiesWithDetails.then(function(query) {
        var restPath = '/users/'+ userUid +'/datasets/' + datasetUuid;
        if(datasetVersionUuid) {
          restPath = restPath + '/versions/' + datasetVersionUuid;
        }
        return $http.get(
          restPath + '/query', {
            params: {
              query: query
            },
            headers: {
              Accept: 'application/sparql-results+json'
            },
            transformResponse: function(data) {
              return deFusekify(data);
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
