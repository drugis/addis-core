'use strict';
define(['angular', 'lodash'],
  function(angular, _) {
    var dependencies = [
      '$scope',
      '$modalInstance',
      'ProjectResource',
      'project',
      'otherProjectNames',
      'callback'
    ];
    var EditProjectController = function(
      $scope,
      $modalInstance,
      ProjectResource,
      project,
      otherProjectNames,
      callback
    ) {
      // functions
      $scope.isNameTaken = isNameTaken;
      $scope.editProject = editProject;
      $scope.cancel = cancel;

      // init
      $scope.project = angular.copy(project);

      function isNameTaken(proposedName) {
        return _.some(otherProjectNames, function(name) {
          return name === proposedName;
        });
      }

      function editProject() {
        $scope.isEditing = true;
        var editProjectCommand = {
          name: $scope.project.name,
          description: $scope.project.description
        };
        ProjectResource.save({
          projectId: $scope.project.id,
        }, editProjectCommand, function() {
          callback($scope.project.name, $scope.project.description);
        });
        $modalInstance.close();
      }

      function cancel() {
        $modalInstance.close();
      }
    };
    return dependencies.concat(EditProjectController);
  });
