'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$modal'];
    var DatasetsController = function($scope, $modal) {

      $scope.items = ['item1', 'item2', 'item3'];

      function onDatasetCreation(dataset) {
        console.log('dataset created: ' + dataset.uri);
        $scope.dataset = dataset;
      };

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
