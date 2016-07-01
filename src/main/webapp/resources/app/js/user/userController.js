'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$scope', '$filter', '$window',
      '$stateParams', '$state', 'UserResource', 'md5'
    ];

    var UserController = function($scope, $filter, $window,
      $stateParams, $state, UserResource, md5) {
      $scope.stripFrontFilter = $filter('stripFrontFilter');
      $scope.otherUsers = [];
      $scope.userUid = Number($stateParams.userUid);
      $scope.loginUser = $window.config.user;

      if (!$scope.activetab) {
        $scope.activetab = $state.current.name;
      }

      $scope.$on("$stateChangeSuccess", function(event, currentState) {
        $scope.activetab = currentState.name;
      });

      $scope.selectTab = function(tab) {
        if ($state.current.name !== tab) {
          $scope.activetab = tab;
          $state.go(tab, {
            userUid: $stateParams.userUid
          });
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
