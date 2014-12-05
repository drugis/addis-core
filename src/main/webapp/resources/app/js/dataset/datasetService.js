'use strict';
define(['rdfstore'], function(rdfstore) {
  var dependencies = ['$q'];
  var DatasetService = function($q) {


    var that = this;
    that.query =
      ' PREFIX dc: <http://purl.org/dc/elements/1.1/>' +
      ' PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>' +
      ' PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' +
      ' PREFIX dataset: <http://trials.drugis.org/datasets/>' +
      ' PREFIX ontology: <http://trials.drugis.org/ontology#>' +
      ' SELECT' +
      ' ?datasetUri ?label ?comment' +
      ' WHERE { ' +
      '   ?datasetUri rdfs:label ?label ; ' +
      '     rdf:type ontology:Dataset . ' +
      '   OPTIONAL { ?datasetUri rdfs:comment ?comment . } ' +
      ' }';

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

    function queryDatasetsOverview() {
      var defer = $q.defer();

      that.store.execute(that.query, function(success, results) {
        if (success) {
          defer.resolve(results);
        } else {
          console.error('dataset query failed!');
          defer.reject();
        }
      });
      return defer.promise;
    }

    function queryDataset() {
      var defer = $q.defer();

      that.store.execute(that.query, function(success, results) {
        if (success) {
          results.length === 1 ? defer.resolve(results[0]) : defer.reject('single result expected');
        } else {
          console.error('dataset query failed!');
          defer.reject();
        }
      });
      return defer.promise;
    }

    function addStudyToDatasetGraph(datasetUUID, studyUUID) {
      var defer = $q.defer();

      var query =
        'PREFIX ontology: <http://trials.drugis.org/ontology#>' +
        'PREFIX study: <http://trials.drugis.org/studies/>' +
        'PREFIX dataset: <http://trials.drugis.org/datasets/>' +

        ' INSERT DATA {' +
        '  dataset:' + datasetUUID + ' ontology:contains_study study:' + studyUUID +
        ' }';

      that.store.execute(query, function(success, result) {
        if (success) {
          console.log('create study update dataset success');
          defer.resolve(result);
        } else {
          console.error('create study update dataset failed');
        }
      });

      return defer.promise;
    }

    function exportGraph() {
      var defer = $q.defer();

      that.store.graph(function(success, graph) {
        defer.resolve(graph.toNT());
      });
      return defer.promise;
    }

    return {
      loadStore: loadStore,
      queryDatasetsOverview: queryDatasetsOverview,
      queryDataset: queryDataset,
      addStudyToDatasetGraph: addStudyToDatasetGraph,
      exportGraph: exportGraph
    };
  };

  return dependencies.concat(DatasetService);
});
