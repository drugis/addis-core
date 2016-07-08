'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$scope', 'UserResource', 'md5'];

    var HomeController = function($scope, UserResource, md5) {

      $scope.users = [];

      UserResource.query(function(users) {
        _.each(users, function(user) {
          user.md5 = md5.createHash(user.email);
          $scope.users.push(user);
        });
      });
    };

    return dependencies.concat(HomeController);
  });
