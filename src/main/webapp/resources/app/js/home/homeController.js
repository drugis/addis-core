'use strict';
define(['lodash'],
  function(_) {
    var dependencies = ['$scope', 'UserResource'];

    var HomeController = function($scope, UserResource) {
      $scope.users = UserResource.query();
    };

    return dependencies.concat(HomeController);
  });
