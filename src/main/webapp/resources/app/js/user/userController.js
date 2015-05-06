'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$location', '$q', '$modal', '$filter', '$window',
      '$stateParams', 'DatasetResource', 'DatasetService', 'UserResource', 'md5'
    ];
    var UserController = function($scope, $location, $q, $modal, $filter, $window,
      $stateParams, DatasetResource, DatasetService, UserResource, md5) {
      $scope.otherUsers = [];
      $scope.userUid = $stateParams.userUid;

      // if no user is supplied, then go to the logedin user user-page
      if (!$scope.userUid || $scope.userUid.length === 0) {
        $location.path('/users/' + $window.config.user.userNameHash);
      }

      UserResource.query(function(users) {
        _.each(users, function(user) {
          user.md5 = md5.createHash(user.username);
          if ($scope.userUid === user.hashedUserName) {
            $scope.user = user;
          } else {
            $scope.otherUsers.push(user);
          }
        });
      });


      function loadDatasets() {
        DatasetService.reset();
        DatasetResource.query(function(response) {
          DatasetService.loadStore(response.data).then(function() {
            console.log('loading dataset-store success');
            DatasetService.queryDatasetsOverview().then(function(datasets) {
              $scope.datasets = datasets;
              $scope.datasetsLoaded = true;
              if (datasets.length > 0) {
                $scope.versionUrlBase = datasets[0].headVersion.split('/').slice(0, 4).join('/') + '/';
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