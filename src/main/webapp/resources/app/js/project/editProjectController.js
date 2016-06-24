'use strict';
define(['angular', 'lodash'],
  function(angular, _) {
    var dependencies = ['$scope', '$modalInstance', 'ProjectResource', 'project', 'otherProjectNames', 'callback'];
    var EditProjectController = function($scope, $modalInstance, ProjectResource, project, otherProjectNames, callback) {

      $scope.project = angular.copy(project);

      $scope.isNameTaken = function(proposedName) {
        return !!_.find(otherProjectNames, function(name){
          return name === proposedName;
        });
      };

      $scope.editProject = function() {
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
      };
      $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
      };
    };
    return dependencies.concat(EditProjectController);
  });
