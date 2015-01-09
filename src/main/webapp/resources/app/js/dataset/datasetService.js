'use strict';
define([], function() {
  var dependencies = ['$q', 'RemoteRdfStoreService'];
  var DatasetService = function($q, RemoteRdfStoreService) {

    var datasetPrefix = 'http://trials.drugis.org/datasets/';
    var scratchDatasetUri;


    function loadStore(data) {
      return RemoteRdfStoreService.create(datasetPrefix).then(function(graphUri) {
        scratchDatasetUri = graphUri;
        return RemoteRdfStoreService.load(scratchDatasetUri, data);
      });
    }

    function queryDatasetsOverview() {
    var query =
      ' PREFIX dc: <http://purl.org/dc/elements/1.1/>' +
      ' PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' +
      ' PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' +
      ' PREFIX dataset: <http://trials.drugis.org/datasets/>' +
      ' PREFIX ontology: <http://trials.drugis.org/ontology#>' +
      ' SELECT' +
      ' ?datasetUri ?label ?comment' +
      ' WHERE { graph <' + scratchDatasetUri + '> {' +
      '   ?datasetUri rdfs:label ?label ; ' +
      '     rdf:type ontology:Dataset . ' +
      '   OPTIONAL { ?datasetUri rdfs:comment ?comment . } ' +
      ' } }';
      return RemoteRdfStoreService.executeQuery(scratchDatasetUri, query);
    }

    function queryDataset() {
    var query =
      ' PREFIX dc: <http://purl.org/dc/elements/1.1/>' +
      ' PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' +
      ' PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' +
      ' PREFIX dataset: <http://trials.drugis.org/datasets/>' +
      ' PREFIX ontology: <http://trials.drugis.org/ontology#>' +
      ' SELECT' +
      ' ?datasetUri ?label ?comment' +
      ' WHERE { graph <' + scratchDatasetUri + '> {' +
      '   ?datasetUri rdfs:label ?label ; ' +
      '     rdf:type ontology:Dataset . ' +
      '   OPTIONAL { ?datasetUri rdfs:comment ?comment . } ' +
      ' } }';
      return RemoteRdfStoreService.executeQuery(scratchDatasetUri, query);
    }

    function addStudyToDatasetGraph(datasetUUID, studyUUID) {
      var query =
        'PREFIX ontology: <http://trials.drugis.org/ontology#>' +
        'PREFIX study: <http://trials.drugis.org/studies/>' +
        'PREFIX dataset: <http://trials.drugis.org/datasets/>' +

        ' INSERT DATA { GRAPH <' + scratchDatasetUri + '>{' +
        '  dataset:' + datasetUUID + ' ontology:contains_study study:' + studyUUID +
        ' }}';

      return RemoteRdfStoreService.executeUpdate(scratchDatasetUri, query);
    }

    function getDatasetGraph() {
      return RemoteRdfStoreService.getGraph(scratchDatasetUri);
    }

    return {
      loadStore: loadStore,
      queryDatasetsOverview: queryDatasetsOverview,
      queryDataset: queryDataset,
      addStudyToDatasetGraph: addStudyToDatasetGraph,
      getDatasetGraph: getDatasetGraph
    };
  };

  return dependencies.concat(DatasetService);
});
