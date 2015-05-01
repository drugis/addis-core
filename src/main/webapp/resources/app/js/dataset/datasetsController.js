'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$location', '$q', '$modal', '$filter', '$window', '$stateParams', 'DatasetResource', 'DatasetService'];
    var DatasetsController = function($scope, $location, $q, $modal, $filter, $window, $stateParams, DatasetResource, DatasetService) {

      $scope.userUid = $stateParams.userUid;
      if(!$scope.userUid) {
        $location.path('/users/' + $window.config.user.userMd5);
      }

      function loadDatasets() {
        DatasetService.reset();
        DatasetResource.query(function(response) {
          DatasetService.loadStore(response.data).then(function() {
            console.log('loading dataset-store success');
            DatasetService.queryDatasetsOverview().then(function(datasets) {
              $scope.datasets = datasets;
              $scope.datasetsLoaded = true;
              if (datasets.length > 0) {
                $scope.versionUrlBase = datasets[0].headVersion.split('/').slice(0,4).join('/') + '/';
              }
            }, function() {
              console.error('failed loading datasetstore');
            });
          });
        });
      }

      loadDatasets();

      $scope.createDatasetDialog = function() {
        $modal.open({
          templateUrl: 'app/js/dataset/createDataset.html',
          controller: 'CreateDatasetController',
          resolve: {
            callback: function() {
              return loadDatasets;
            }
          }
        });
      };

      $scope.stripFrontFilter = $filter('stripFrontFilter');

    };
    return dependencies.concat(DatasetsController);
  });


