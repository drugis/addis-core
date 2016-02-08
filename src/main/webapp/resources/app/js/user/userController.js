'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$scope', '$filter', '$window',
      '$stateParams', '$state', 'UserResource', 'md5'
    ];

    var UserController = function($scope, $filter, $window,
      $stateParams, $state, UserResource, md5) {
      console.log('user controller');
      $scope.stripFrontFilter = $filter('stripFrontFilter');
      $scope.otherUsers = [];
      $scope.userUid = Number($stateParams.userUid);
      $scope.loginUser = $window.config.user;

      if(!$scope.activetab) {
        console.log('set active as no other tab is');
        console.log('state is ' + $state.current.name);
        $scope.activetab = $state.current.name;
      }

      $scope.selectTab = function(tab) {
        if ($state.current.name !== tab) {
          $scope.activetab = tab;
          $state.go(tab, {userUid: $stateParams.userUid});
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
