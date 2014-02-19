'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', 'ProjectsService'];
  var ProjectsController = function($scope, $stateParams, ProjectsService) {
    $scope.project = ProjectsService.query($stateParams);

  };
  return dependencies.concat(ProjectsController);
});
