'use strict';
define(['rdfstore-js'],
  function() {
    var dependencies = ['$scope', '$modal', 'DatasetResource'];
    var DatasetsController = function($scope, $modal, DatasetResource) {

      var query =
      'prefix dc: <http://purl.org/dc/elements/1.1/>' +
      'prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>' +
      'prefix dataset: <http://trials.drugis.org/datasets/>' +
        'select' +
        '  ?title ?description ?creator ' +
        'where { ' +
        ' ?datasetUri dc:creator ?creator;' +
        '   rdfs:label ?title;' +
        '   rdfs:comment ?description }';

// prefix dc: <http://purl.org/dc/elements/1.1/>
// prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
// prefix dataset: <http://trials.drugis.org/datasets/>
// prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
// prefix ontology: <http://trials.drugis.org/ontology#>
      $scope.testOutput = {
        data: []
      };

      DatasetResource.query(function(result) {

        window.rdfstore.create(function(store) {
          store.load('text/turtle', result.graphData, function(isSuccess, b) {
            console.log('succes ? ' + isSuccess);
            console.log('number of triples ' + b);
            store.setPrefix('dc', 'http://purl.org/dc/elements/1.1/');
            store.setPrefix('rdfs', 'http://www.w3.org/2000/01/rdf-schema#');
            store.execute(query, function(isSuccessFullQuery, result) {
              $scope.testOutput.data = result;
              $scope.$apply();
            });
          });
        });

      });

      function onDatasetCreation(dataset) {
        console.log('dataset created: ' + dataset.uri);
        $scope.dataset = dataset;
      }

      $scope.createDatasetDialog = function() {
        $modal.open({
          templateUrl: 'app/js/dataset/createDataset.html',
          controller: 'CreateDatasetController',
          resolve: {
            successCallback: function() {
              return onDatasetCreation;
            }
          }
        });
      };
    };
    return dependencies.concat(DatasetsController);
  });