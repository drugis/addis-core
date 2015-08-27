'use strict';
define([], function() {
  var dependencies = ['$q', 'DatasetService'];
  var DatasetService = function($q, DatasetService) {

    var loadDefer = $q.defer();
    var scratchUri;

    function loadStore(data) {
      return DatasetService.loadStore(data, function(newGraphUri) {
        scratchUri = newGraphUri;
      }).then(function() {
        loadDefer.resolve();
      });
    }

    function queryDataset() {
      return loadDefer.promise.then(function() {
        var query =
          ' PREFIX dcterms: <http://purl.org/dc/terms/> ' +
          ' PREFIX void: <http://rdfs.org/ns/void#> ' +
          ' SELECT' +
          ' ?datasetUri ?label ?comment ?creator' +
          ' WHERE { graph <' + scratchUri + '> {' +
          '   ?datasetUri dcterms:title ?label ; ' +
          '     dcterms:creator ?creator ; ' +
          '     a void:Dataset . ' +
          '   OPTIONAL { ?datasetUri dcterms:description ?comment . } ' +
          ' } }';
        return DatasetService.executeQuery(scratchUri, query);
      });
    }

    function addStudyToDatasetGraph(datasetUUID, studyUUID) {
      return loadDefer.promise.then(function() {
        var query =
          'PREFIX ontology: <http://trials.drugis.org/ontology#>' +
          'PREFIX study: <http://trials.drugis.org/studies/>' +
          'PREFIX dataset: <http://trials.drugis.org/datasets/>' +

          ' INSERT DATA { GRAPH <' + scratchUri + '> {' +
          '  dataset:' + datasetUUID + ' ontology:contains_study study:' + studyUUID +
          ' }}';

        return DatasetService.executeUpdate(scratchUri, query);
      });
    }

    function getDatasetGraph() {
      return loadDefer.promise.then(function() {
        return DatasetService.getGraph(scratchUri);
      });
    }

    function reset() {
      loadDefer = $q.defer();
    }

    return {
      loadStore: loadStore,
      queryDataset: queryDataset,
      addStudyToDatasetGraph: addStudyToDatasetGraph,
      getDatasetGraph: getDatasetGraph,
      reset: reset
    };
  };

  return dependencies.concat(DatasetService);
});
