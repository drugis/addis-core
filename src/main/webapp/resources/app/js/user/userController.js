'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$scope', '$filter', 'UserService',
      '$stateParams', '$state', 'UserResource', 'md5'
    ];
    var UserController = function($scope, $filter, UserService,
      $stateParams, $state, UserResource, md5) {
      // functions
      $scope.selectTab = selectTab;

      // init
      $scope.stripFrontFilter = $filter('stripFrontFilter');
      $scope.otherUsers = [];
      $scope.userUid = Number($stateParams.userUid);

      if (UserService.hasLoggedInUser()) {
        $scope.loginUser = UserService.getLoginUser;
      }

      if (!$scope.activetab) {
        $scope.activetab = $state.current.name;
      }

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

      function selectTab(tab) {
        if ($state.current.name !== tab) {
          $scope.activetab = tab;
          $state.go(tab, {
            userUid: $stateParams.userUid
          });
        }
      }

    };
    return dependencies.concat(UserController);
  });
