'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$location', '$q', '$modal', '$filter', '$window',
     '$stateParams', 'DatasetResource', 'DatasetService', 'UserResource'];
    var UserController = function($scope, $location, $q, $modal, $filter, $window,
     $stateParams, DatasetResource, DatasetService, UserResource) {

      $scope.userUid = $stateParams.userUid;
      if(!$scope.userUid) {
        $location.path('/users/' + $window.config.user.userMd5);
      }

      $scope.user = UserResource.get({userUid: $window.config.user.userNameHash}); 

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
          templateUrl: 'app/js/user/createDataset.html',
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
    return dependencies.concat(UserController);
  });


