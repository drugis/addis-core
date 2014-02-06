'use strict';
define(['angular'], function(angular) {
  var dependencies = ['$scope'];
  var ProjectsController = function($scope) {

    $scope.foobar = {label:"Hallo"};

  };
  return dependencies.concat(ProjectsController);
});
