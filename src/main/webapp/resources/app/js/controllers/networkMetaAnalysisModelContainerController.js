'use strict';
define([], function() {
  var dependencies = ['$scope', '$q', '$state', 'ProjectResource', 'AnalysisResource', 'UserService'];

  var NetworkMetaAnalysisModelContainerController = function($scope, $q, $state, ProjectResource, AnalysisResource, UserService) {
    $scope.project = ProjectResource.get({
      projectId: $state.params.projectId
    });
    $scope.analysis = AnalysisResource.get($state.params);
    $scope.userId = $state.params.userUid;
    $scope.userUid = $state.params.userUid;
    $scope.editMode = {
      disableEditing: true
    };
    var isUserOwner = false;

    $q.all([$scope.project.$promise, $scope.analysis.$promise]).then(function() {
      if (UserService.hasLoggedInUser()) {
        $scope.loginUserId = (UserService.getLoginUser()).id;
        isUserOwner = UserService.isLoginUserId($scope.project.owner.id);
      }

      $scope.editMode = {
        isUserOwner: isUserOwner,
        disableEditing: !isUserOwner || $scope.project.archived || $scope.analysis.archived
      };
    });
  };

  return dependencies.concat(NetworkMetaAnalysisModelContainerController);
});
