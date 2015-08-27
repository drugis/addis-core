'use strict';
define([], function() {
  var dependencies = ['$q', 'RemoteRdfStoreService'];
  var DatasetService = function($q, RemoteRdfStoreService) {

    var datasetPrefix = 'http://trials.drugis.org/datasets/';

    function loadStore(data, scratchUriCallback) {
      return RemoteRdfStoreService.create(datasetPrefix).then(function(graphUri) {
        scratchUriCallback(graphUri);
        return RemoteRdfStoreService.load(graphUri, data);
      });
    }

    function executeQuery(scratchUri, query) {
      return RemoteRdfStoreService.executeQuery(scratchUri, query);
    }

    function executeUpdate(scratchUri, query) {
      return RemoteRdfStoreService.executeUpdate(scratchUri, query);
    }

    function getDatasetGraph(scratchUri) {
      return RemoteRdfStoreService.getGraph(scratchUri);
    }

    return {
      loadStore: loadStore,
      executeQuery: executeQuery,
      executeUpdate: executeUpdate,
      getDatasetGraph: getDatasetGraph
    };
  };

  return dependencies.concat(DatasetService);
});
