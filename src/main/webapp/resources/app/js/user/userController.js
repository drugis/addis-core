'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$scope', '$location', '$q', '$modal', '$filter', '$window',
      '$stateParams', '$state', 'UserResource', 'md5'
    ];

    var UserController = function($scope, $location, $q, $modal, $filter, $window,
      $stateParams, $state, UserResource, md5) {
      $scope.stripFrontFilter = $filter('stripFrontFilter');
      $scope.otherUsers = [];
      $scope.userUid = Number($stateParams.userUid);
      $scope.loginUser = $window.config.user;

      $scope.user = $window.config.user;

      if(!$scope.activetab) {
        $scope.activetab = 'projects';
      }

      $scope.selectProjectsTab = function() {
        if ($state.current.name !== 'projects') {
          $scope.activetab = 'projects';
          $state.go('projects', {userUid: $stateParams.userUid});
        }
      };

      $scope.selectDatasetsTab = function() {
        if ($state.current.name !== 'datasets') {
          $scope.activetab = 'datasets';
          $state.go('datasets', {userUid: $stateParams.userUid});
        }
      };

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

    };
    return dependencies.concat(UserController);
  });
