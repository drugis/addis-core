'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', 'ProjectsService'];
  var ProjectsController = function($scope, $stateParams, ProjectsService) {

    $scope.loading = {loaded : false};
    $scope.project = ProjectsService.get($stateParams);
    $scope.project.$promise.then(function() {
      $scope.loading.loaded = true;
    });
  };
  return dependencies.concat(ProjectsController);
});
