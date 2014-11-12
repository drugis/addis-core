'use strict';
define(['rdfstore', 'lodash'],
  function(rdfstore, _) {
    var dependencies = ['$scope', '$modal', 'DatasetService'];
    var DatasetsController = function($scope, $modal, DatasetService) {

      DatasetService.loadDatasets();

      function onDatasetCreation(dataset) {
        DatasetService.loadDatasets();
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
