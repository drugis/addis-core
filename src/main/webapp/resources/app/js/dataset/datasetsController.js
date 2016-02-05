'use strict';
define([], function() {
    var dependencies = ['$scope', '$location', '$q', '$modal', '$filter', '$window',
      '$stateParams', 'DatasetResource'
    ];

    var DatasetsController = function($scope, $location, $q, $modal, $filter, $window,
      $stateParams, DatasetResource) {
      console.log('datasets controller');
      $scope.stripFrontFilter = $filter('stripFrontFilter');
      $scope.otherUsers = [];
      $scope.userUid = Number($stateParams.userUid);
      $scope.loginUser = $window.config.user;
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
