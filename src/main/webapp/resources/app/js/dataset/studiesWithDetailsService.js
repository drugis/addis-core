'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$http', 'SparqlResource'];
  var StudiesWithDetailsService = function($http, SparqlResource) {

    var queryStudiesWithDetails = SparqlResource.get('queryStudiesWithDetails.sparql');
    var getStudyTitleQuery = SparqlResource.get('getStudyTitleQuery.sparql');

    function deFusekify(data) {
      var json = JSON.parse(data);
      var bindings = json.results.bindings;
      return _.map(bindings, function(binding) {
        return _.fromPairs(_.map(_.toPairs(binding), function(obj) {
          return [obj[0], obj[1].value];
        }));
      });
    }

    function executeQuery(queryPromise, userUid, datasetUuid, datasetVersionUuid) {
      return queryPromise.then(function(query) {
        var restPath = '/users/' + userUid + '/datasets/' + datasetUuid;
        if (datasetVersionUuid) {
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


    function get(userUid, datasetUuid, datasetVersionUuid) {
      return executeQuery(queryStudiesWithDetails, userUid, datasetUuid, datasetVersionUuid);
    }

    function getWithoutDetails(userUid, datasetUuid, datasetVersionUuid, studyUuid) {
      var filledInQuery = getStudyTitleQuery.then(function(query) {
        return query.replace('$studyUuid$', studyUuid);
      });
      return executeQuery(filledInQuery, userUid, datasetUuid, datasetVersionUuid);
    }

    return {
      get: get,
      getWithoutDetails: getWithoutDetails
    };
  };
  return dependencies.concat(StudiesWithDetailsService);
});
