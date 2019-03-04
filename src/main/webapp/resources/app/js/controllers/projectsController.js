'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = [
    '$scope', '$stateParams',
    'ProjectResource',
    'UserService',
    'PageTitleService'
  ];
  var ProjectsController = function(
    $scope, $stateParams,
    ProjectResource,
    UserService,
    PageTitleService
  ) {
    // functions
    $scope.archiveProject = archiveProject;
    $scope.unarchiveProject = unarchiveProject;
    $scope.toggleShowArchived = toggleShowArchived;

    //init
    $scope.$watch('user', function(user) {
      if (user && user.id) {
        PageTitleService.setPageTitle('ProjectsController', user.firstName + ' ' + user.lastName + '\'s projects');
      }
    });
    $scope.editMode = {
      allowEditing: false
    };
    $scope.userId = Number($stateParams.userUid);
    $scope.showArchived = false;
    $scope.numberOfProjectsArchived = 0;
    loadProjects();

    function archiveProject(project) {
      var params = {
        projectId: project.id
      };
      ProjectResource.setArchived(
        params, {
          isArchived: true
        }
      ).$promise.then(loadProjects);
    }

    function unarchiveProject(project) {
      var params = {
        projectId: project.id
      };
      ProjectResource.setArchived(
        params, {
          isArchived: false
        }
      ).$promise.then(loadProjects);
    }

    function toggleShowArchived() {
      $scope.showArchived = !$scope.showArchived;
    }

    function loadProjects() {
      $scope.projects = ProjectResource.query({
        owner: $scope.userId
      });
      $scope.projects.$promise.then(function() {
        $scope.projects = _.sortBy($scope.projects, ['archived', 'id']);
        $scope.numberOfProjectsArchived = _.reduce($scope.projects, function(accum, project) {
          return project.archived ? ++accum : accum;
        }, 0);
        if ($scope.numberOfProjectsArchived === 0) {
          $scope.showArchived = false;
        }
      });
      UserService.isLoginUserId($scope.userId).then(function(isUserOwner){
        $scope.editMode.allowEditing = isUserOwner;
      });
    }
  };
  return dependencies.concat(ProjectsController);
});
