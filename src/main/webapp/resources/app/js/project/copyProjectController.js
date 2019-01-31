'use strict';
define([], function() {
  var dependencies = [
    '$scope',
    '$modalInstance',
    'ProjectResource',
    'callback'
  ];
  var CopyProjectController = function(
    $scope,
    $modalInstance,
    ProjectResource, callback
  ) {
    //functions
    $scope.copyProject = copyProject;
    $scope.cancel = cancel;

    //init
    $scope.isCopying = false;

    function copyProject(newTitle) {
      $scope.isCopying = true;
      ProjectResource.copy({
        projectId: $scope.project.id
      }, {
          newTitle: newTitle
        }, function(response) {
          callback(response.newProjectId);
          $modalInstance.close();
        });
    }

    function cancel() {
      $modalInstance.close();
    }
  };
  return dependencies.concat(CopyProjectController);
});
