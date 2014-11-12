'use strict';
var DatasetService = define(['angular'], function() {
  var dependencies = ['$q', 'DatasetResource'];

  var DatasetService = function($q, DatasetResource) {
    var datasetQuery =
      'prefix dc: <http://purl.org/dc/elements/1.1/>' +
      'prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>' +
      'prefix dataset: <http://trials.drugis.org/datasets/>' +
      'select' +
      '  ?datasetUri ?title ?description ?creator ' +
      'where { ' +
      ' ?datasetUri dc:creator ?creator;' +
      '   rdfs:label ?title;' +
      '   rdfs:comment ?description }';

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
      var datasets = $q.defer();
      DatasetResource.query(function(resourceResult) {

        rdfstore.create(function(store) {
          store.load('text/turtle', resourceResult.graphData, function() {
            store.execute(datasetQuery, function(success, result) {
              datasets.resolve(attachUUIDs(result));
            });
          });
        });

      });
      return datasets;
    }

    return {
      getDatasets: getDatasets
    };
  };

  return dependencies.concat(DatasetService);
});
