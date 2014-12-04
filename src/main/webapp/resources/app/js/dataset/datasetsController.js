'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$q', '$modal', 'DatasetResource', 'DatasetService'];
    var DatasetsController = function($scope, $q, $modal, DatasetResource, DatasetService) {

      DatasetResource.query(function(responce) {
        DatasetService.loadStore(responce.n3Data).then(function(numberOfTriples) {
          console.log('loading dataset-store success, ' + numberOfTriples + ' triples loaded');
          DatasetService.queryDatasets().then(function(queryResult) {
            $scope.datasets = queryResult;
          }, function(){
            console.error('failed loading datasetstore')
          });
        });
      });

      function loadDatasets() {
        var datasetsPromise = DatasetService.getDatasets();
        datasetsPromise.promise.then(function(datasets) {
          $scope.datasets = datasets;
        });
      }

      function onDatasetCreation(dataset) {
        loadDatasets();
      }

      // loadDatasets();

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