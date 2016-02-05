'use strict';
define([], function() {
	var dependencies = ['$scope', '$state', 'ProjectResource', 'dataset'];
	var CreateProjectModalController = function($scope, $state, ProjectResource, dataset) {

    $scope.dataset = dataset;

    $scope.createProject = function(newProject) {
      this.model = {};  // clear modal form by resetting model in current scope
      newProject.namespaceUid = dataset.uid;
      newProject.datasetVersion = dataset.version;
      ProjectResource.save(newProject, function(savedProject) {
        $state.go('project', {
          projectId: savedProject.id
        });
      });
    };
	};
	return dependencies.concat(CreateProjectModalController);
});
