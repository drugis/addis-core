'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$q', '$modal', 'DatasetService'];
    var DatasetsController = function($scope, $q, $modal, DatasetService) {

      function loadDatasets() {
        var datasetsPromise = DatasetService.getDatasets();
        datasetsPromise.promise.then(function(datasets) {
          $scope.datasets = datasets;
        });
      }

      function onDatasetCreation(dataset) {
        loadDatasets();
      }

      loadDatasets();

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
