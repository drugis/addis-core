'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', '$modalInstance', 'ProjectResource', 'callback'];
  var UpdateProjectController = function($scope, $stateParams, $modalInstance, ProjectResource, callback) {
    // functions
    $scope.updateProject = updateProject;
    $scope.cancel = cancel;

    // init
    $scope.isUpdating = false;

    function updateProject() {
      $scope.isUpdating = true;
      ProjectResource.update($scope.project.id, {
        projectId: $scope.project.id
      }, function(response) {
        callback(response.newProjectId);
        $modalInstance.close();
      });
    }

    function cancel() {
      $modalInstance.close();
    }
  };
  return dependencies.concat(UpdateProjectController);
});