'use strict';
define([], function() {
  var dependencies = ['$http', 'UUIDService', 'FUSEKI_STORE_URL'];
  var RemotestoreService = function($http, UUIDService, FUSEKI_STORE_URL) {

    function create(uriBase) {
      var graphUri = uriBase + UUIDService.generate();
      var query = 'CREATE GRAPH <' + graphUri + '>';
      return executeUpdate('default', query).then(function() {
        return graphUri;
      });
    }

    function load(graphUri, data) {
      return $http({
        url: FUSEKI_STORE_URL + '/data',
        method: 'POST',
        data: data,
        params: {
          'graph': graphUri
        },
        headers: {
          'Content-Type': 'text/turtle',
          'Accept': 'application/ld+json'
        }
      });
    }

    function executeUpdate(graphUri, query) {
      return $http({
        url: FUSEKI_STORE_URL + '/update',
        method: 'POST',
        data: query,
        params: {
          'default-graph-uri': graphUri,
          'output': 'json'
        },
        headers: {
          'Content-Type': 'application/sparql-update',
          'Accept': 'application/ld+json'
        }
      });
    }

    function executeQuery(graphUri, query) {
      return $http({
        url: FUSEKI_STORE_URL + '/query',
        method: 'POST',
        data: query,
        params: {
          'default-graph-uri': graphUri,
          'output': 'json'
        },
        headers: {
          'Content-Type': 'application/sparql-query',
          'Accept': 'application/ld+json'
        }
      });
    }

    return {
      create: create,
      load: load,
      executeUpdate: executeUpdate,
      executeQuery: executeQuery
    };
  };
  return dependencies.concat(RemotestoreService);
});