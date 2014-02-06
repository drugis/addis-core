'use strict';
define([], function() {
  var dependencies = ['$scope', 'ProjectsService'];
  var ProjectsController = function($scope, ProjectsService) {

    $scope.foobar = {label:"Hallo"};
    $scope.projects = ProjectsService.query();
  };
  return dependencies.concat(ProjectsController);
});
