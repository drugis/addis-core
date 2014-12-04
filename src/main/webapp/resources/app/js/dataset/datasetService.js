'use strict';
define(['rdfstore'], function(rdfstore) {
  var dependencies = ['$q', 'DatasetResource', 'RdfstoreService'];
  var DatasetService = function($q, DatasetResource, RdfstoreService) {

    var store;

    function findUUIDFromString(str) {
      return str.substr(str.lastIndexOf('/') + 1);
    }

    function attachUUIDs(datasets) {
      return _.map(datasets, function(dataset) {
        dataset.uuid = findUUIDFromString(dataset.datasetUri.value);
        return dataset;
      });
    }

    function loadStore(data) {
        var defer = $q.defer();
        var that = this;
        rdfstore.create(function(store) {
          that.store = store;
          that.store.load('text/n3', data, function(success, results) {
            if (success) {
              defer.resolve(results);
            } else {
              console.error('failed loading store');
              defer.reject();
            }
          });
        });
        return defer.promise;
      }

    function getDatasets() {
      var promiseHolder = $q.defer();
      var datasetsQuery =
        'prefix dc: <http://purl.org/dc/elements/1.1/>' +
        'prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>' +
        'prefix dataset: <http://trials.drugis.org/datasets/>' +
        'select' +
        '  ?datasetUri ?title ?description ?creator ' +
        'where { ' +
        '   ?datasetUri rdfs:label ?title .' +
        '   OPTIONAL {?datasetUri dc:creator ?creator .}' +
        '   OPTIONAL {?datasetUri rdfs:comment ?description .} }';

      DatasetResource.query().$promise.then(function(resourceResult) {

        RdfstoreService.load(datasetGraphStore, resourceResult.graphData)
          .promise.then(function(datasetGraphStore) {
            RdfstoreService.execute(datasetGraphStore, datasetsQuery)
              .promise.then(function(result) {
                promiseHolder.resolve(attachUUIDs(result));
              });
          });
      });
      return promiseHolder;
    }

    function addStudyToDatasetGraph(studyUUID) {
      var defer = $q.defer();
      RdfstoreService.execute(datasetGraphStore, datasetQuery).promise.then(function(result) {
        var newGraph = angular.copy(result);

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
        defer.resolve(newGraph);
      });
      return defer.promise;
    }

    return {
      loadStore: loadStore,
      getDatasets: getDatasets,
      addStudyToDatasetGraph: addStudyToDatasetGraph
    };
  };

  return dependencies.concat(DatasetService);
});