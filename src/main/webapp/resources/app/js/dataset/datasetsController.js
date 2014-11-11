'use strict';
define(['rdfstore-js'],
  function() {
    var dependencies = ['$scope', '$modal', 'DatasetResource'];
    var DatasetsController = function($scope, $modal, DatasetResource) {

      $scope.testOutput = {
        data: []
      };

      DatasetResource.query(function(result) {

        window.rdfstore.create(function(store) {
          store.load('text/turtle', result.graphData, function(isSuccess, b) {
            console.log('succes ? ' + isSuccess);
            console.log('number of triples ' + b);
            store.execute('select * where { ?a ?b ?c .}', function(isSuccessFullQuery, result) {
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