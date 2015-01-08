'use strict';
define([], function() {
  var dependencies = ['$http', 'UUIDService', 'FUSEKI_STORE_URL'];
  var RemotestoreService = function($http, UUIDService, FUSEKI_STORE_URL) {

    function create(uriBase) {
      var graphUri = uriBase + UUIDService.generate();
      var query = 'CREATE GRAPH ' + graphUri;
      return execute('default', query);
    }

    function load(graphUri, data) {
      return $http.post(FUSEKI_STORE_URL, data);
    }

    function execute(graphUri, query) {
      return $http({
        url: FUSEKI_STORE_URL,
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
      execute: execute
    };
  };
  return dependencies.concat(RemotestoreService);
});