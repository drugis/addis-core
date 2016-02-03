'use strict';
define([],
  function() {
    var dependencies = ['$scope', '$location', '$q', '$modal', '$filter', '$window',
      '$stateParams', 'DatasetResource', 'UserResource', 'md5'
    ];

    var UserController = function($scope, $location, $q, $modal, $filter, $window,
      $stateParams, DatasetResource, UserResource, md5) {
      $scope.stripFrontFilter = $filter('stripFrontFilter');
      $scope.otherUsers = [];
      $scope.userUid = Number($stateParams.userUid);
      $scope.loginUser = $window.config.user;
      $scope.datasetsLoaded = false;
      $scope.reloadDatasets = reloadDatasets;

      // if no user is supplied, then go to the logged-in user user-page
      if (!$scope.userUid || $scope.userUid.length === 0) {
        $location.path('/users/' + $scope.loginUser.id);
      } else {
        UserResource.query(function(users) {
          _.each(users, function(user) {
            user.md5 = md5.createHash(user.email);
            if ($scope.userUid === user.id) {
              $scope.user = user;
            } else {
              $scope.otherUsers.push(user);
            }
          });
        });
        reloadDatasets();
      }

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
    return dependencies.concat(UserController);
  });
