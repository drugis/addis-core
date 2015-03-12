'use strict';
define([], function() {
  var dependencies = ['$q', 'RemoteRdfStoreService'];
  var DatasetService = function($q, RemoteRdfStoreService) {

    var loadDefer = $q.defer();
    var datasetPrefix = 'http://trials.drugis.org/datasets/';
    var scratchDatasetUri;

    function loadStore(data) {
      return RemoteRdfStoreService.create(datasetPrefix).then(function(graphUri) {
        scratchDatasetUri = graphUri;
        return RemoteRdfStoreService.load(scratchDatasetUri, data).then(function() {
          loadDefer.resolve();
        });
      });
    }

    function queryDatasetsOverview() {
      return loadDefer.promise.then(function() {
        var query =
          ' PREFIX dcterms: <http://purl.org/dc/terms/> ' +
          ' PREFIX void: <http://rdfs.org/ns/void#> ' +
          ' SELECT ' +
          ' ?datasetUri ?label ?comment' +
          ' WHERE { graph <' + scratchDatasetUri + '> {' +
          '   ?datasetUri dcterms:title ?label ; ' +
          '     a void:Dataset . ' +
          '   OPTIONAL { ?datasetUri dcterms:description ?comment . } ' +
          ' } }';
        return RemoteRdfStoreService.executeQuery(scratchDatasetUri, query);
      });
    }

    function queryDataset() {
      return loadDefer.promise.then(function() {
        var query =
          ' PREFIX dcterms: <http://purl.org/dc/terms/> ' +
          ' PREFIX void: <http://rdfs.org/ns/void#> ' +
          ' SELECT' +
          ' ?datasetUri ?label ?comment' +
          ' WHERE { graph <' + scratchDatasetUri + '> {' +
          '   ?datasetUri dcterms:title ?label ; ' +
          '     a void:Dataset . ' +
          '   OPTIONAL { ?datasetUri dcterms:description ?comment . } ' +
          ' } }';
        return RemoteRdfStoreService.executeQuery(scratchDatasetUri, query);
      });
    }

    function addStudyToDatasetGraph(datasetUUID, studyUUID) {
      return loadDefer.promise.then(function() {
        var query =
          'PREFIX ontology: <http://trials.drugis.org/ontology#>' +
          'PREFIX study: <http://trials.drugis.org/studies/>' +
          'PREFIX dataset: <http://trials.drugis.org/datasets/>' +

          ' INSERT DATA { GRAPH <' + scratchDatasetUri + '> {' +
          '  dataset:' + datasetUUID + ' ontology:contains_study study:' + studyUUID +
          ' }}';

        return RemoteRdfStoreService.executeUpdate(scratchDatasetUri, query);
      });
    }

    function getDatasetGraph() {
      return loadDefer.promise.then(function() {
        return RemoteRdfStoreService.getGraph(scratchDatasetUri);
      });
    }

    function reset() {
      loadDefer = $q.defer();
    }

    return {
      loadStore: loadStore,
      queryDatasetsOverview: queryDatasetsOverview,
      queryDataset: queryDataset,
      addStudyToDatasetGraph: addStudyToDatasetGraph,
      getDatasetGraph: getDatasetGraph,
      reset: reset
    };
  };

  return dependencies.concat(DatasetService);
});
