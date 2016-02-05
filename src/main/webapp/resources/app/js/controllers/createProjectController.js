'use strict';
define([], function() {
	var dependencies = ['$scope', '$state', 'ProjectResource', 'TrialverseResource'];
	var CreateProjectController = function($scope, $state, ProjectResource, TrialverseResource) {
    $scope.namespaces = TrialverseResource.query();
    $scope.userId = $state.params.userUid;

    $scope.showCreateProjectModal = function(selectedNamespace) {
      $scope.createProjectModal.selectedNamespace = selectedNamespace;
      $scope.createProjectModal.open();
    };

    $scope.createProject = function(newProject) {
      this.model = {};  // clear modal form by resetting model in current scope
      newProject.namespaceUid = $scope.createProjectModal.selectedNamespace.uid;
      newProject.datasetVersion = $scope.createProjectModal.selectedNamespace.version;
      ProjectResource.save(newProject, function(savedProject) {
        $state.go('project', {
          projectId: savedProject.id
        });
      });
    };
	};
	return dependencies.concat(CreateProjectController);
});
