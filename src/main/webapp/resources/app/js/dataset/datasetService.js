'use strict';
define(['angular'], function(angular) {
  var dependencies = ['$q', 'DatasetResource', 'RdfstoreService'];

  var DatasetService = function($q, DatasetResource, RdfstoreService) {
    var datasetQuery =
      'prefix dc: <http://purl.org/dc/elements/1.1/>' +
      'prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>' +
      'prefix dataset: <http://trials.drugis.org/datasets/>' +
      'select' +
      '  ?datasetUri ?title ?description ?creator ' +
      'where { ' +
      '   ?datasetUri rdfs:label ?title .' +
      '   OPTIONAL {?datasetUri dc:creator ?creator .}' +
      '   OPTIONAL {?datasetUri rdfs:comment ?description .} }';

    var datasetGraphStore;

    function findUUIDFromString(str) {
      return str.substr(str.lastIndexOf('/') + 1);
    }

    function attachUUIDs(datasets) {
      return _.map(datasets, function(dataset) {
        dataset.uuid = findUUIDFromString(dataset.datasetUri.value);
        return dataset;
      });
    }

    function getDatasets() {
      var promiseHolder = $q.defer();
      DatasetResource.query().$promise.then(function(resourceResult) {

        RdfstoreService.load(datasetGraphStore, resourceResult.graphData)
          .promise.then(function(datasetGraphStore) {
            RdfstoreService.execute(datasetGraphStore, datasetQuery)
              .promise.then(function(result) {
                promiseHolder.resolve(attachUUIDs(result));
              });
          });
      });
      return promiseHolder;
    }

    function addStudyToDatasetGraph(studyUUID, datasetGraph) {
      var newGraph = angular.copy(datasetGraph);

      newGraph['@context'].contains_study = {
        '@id': 'http://trials.drugis.org/ontology#contains_study',
        '@type': '@id'
      };
      newGraph['@context'].ontology = 'http://trials.drugis.org/ontology#';
      newGraph['@context'].study = 'http://trials.drugis.org/studies/';
      if (!newGraph.contains_study) {
        newGraph.contains_study = 'study:' + studyUUID;
      } else {
        newGraph.contains_study = ['study:' + studyUUID].concat(newGraph.contains_study);
      }
      return newGraph;
    }

    return {
      getDatasets: getDatasets,
      addStudyToDatasetGraph: addStudyToDatasetGraph
    };
  };

  return dependencies.concat(DatasetService);
});