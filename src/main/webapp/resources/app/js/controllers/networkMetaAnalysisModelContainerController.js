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

    $q.all([UserService.getLoginUser(), $scope.project.$promise, $scope.analysis.$promise]).then(function(results) {
      var user = results[0];
      if (!!user) {
        $scope.loginUserId = user.id;
        isUserOwner = $scope.project.owner.id === user.id;
      }

      $scope.editMode = {
        isUserOwner: isUserOwner,
        disableEditing: !isUserOwner || $scope.project.archived || $scope.analysis.archived
      };
    });
  };

  return dependencies.concat(NetworkMetaAnalysisModelContainerController);
});
