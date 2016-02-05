'use strict';
define([], function() {
    var dependencies = ['$scope', '$modal', '$filter', '$stateParams', 'DatasetResource'
    ];

    var DatasetsController = function($scope, $modal, $filter, $stateParams, DatasetResource) {
      $scope.stripFrontFilter = $filter('stripFrontFilter');
      $scope.datasetsLoaded = false;
      $scope.reloadDatasets = reloadDatasets;

      reloadDatasets();

      function reloadDatasets() {
        $scope.datasetsLoaded = false;
        DatasetResource.queryForJson($stateParams, function(datasets) {
          $scope.datasets = datasets;
          $scope.datasetsLoaded = true;
        });
      }

      $scope.createDatasetDialog = function() {
        $modal.open({
          templateUrl: 'app/js/user/createDataset.html',
          controller: 'CreateDatasetController',
          resolve: {
            callback: function() {
              return reloadDatasets;
            }
          }
        });
      };

    };
    return dependencies.concat(DatasetsController);
  });
