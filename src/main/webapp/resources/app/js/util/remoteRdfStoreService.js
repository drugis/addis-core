'use strict';
define([], function() {
  var dependencies = ['$http', 'UUIDService', 'SCRATCH_RDF_STORE_URL'];
  var RemotestoreService = function($http, UUIDService, SCRATCH_RDF_STORE_URL) {

    function create(uriBase) {
      var graphUri = uriBase + UUIDService.generate();
      var query = 'CREATE GRAPH <' + graphUri + '>';
      return executeUpdate('default', query).then(function() {
        return graphUri;
      });
    }

    function load(graphUri, data) {
      return $http.post(SCRATCH_RDF_STORE_URL + '/data',
        data, {
          params: {
            'graph': graphUri
          },
          headers: {
            'Content-Type': 'text/turtle'
          }
        }
      );
    }

    function executeUpdate(graphUri, query) {
      query = query.replace(/\$graphUri/g, graphUri);

      return $http.post(
        SCRATCH_RDF_STORE_URL + '/update',
        query, {
          params: {
            'output': 'json'
          },
          headers: {
            'Content-Type': 'application/sparql-update'
          }
        });
    }

    function executeQuery(graphUri, query) {
      query = query.replace(/\$graphUri/g, graphUri);

      return $http.post(SCRATCH_RDF_STORE_URL + '/query',
        query, {
          transformResponse: function(data) {
            return deFusekify(data);
          },
          params: {
            'output': 'json'
          },
          headers: {
            'Content-Type': 'application/sparql-query',
            'Accept': 'application/ld+json'
          }
        });
    }

    function getGraph(graphUri) {
      return $http.get(
        SCRATCH_RDF_STORE_URL, {
          params: {
            graph: graphUri
          }
        });
    }

    function deFusekify(data) {
      var bindings = data.data.results.bindings;
      return _.map(bindings, function(binding) {
        return _.object(_.map(_.pairs(binding), function(obj) {
          return [obj[0], obj[1].value];
        }));
      });
    }

    return {
      create: create,
      load: load,
      executeUpdate: executeUpdate,
      executeQuery: executeQuery,
      getGraph: getGraph,
      deFusekify: deFusekify
    };
  };
  return dependencies.concat(RemotestoreService);
});
