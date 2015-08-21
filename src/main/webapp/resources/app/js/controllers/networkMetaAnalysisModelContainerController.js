'use strict';
define([], function() {
  var dependencies = ['$scope', '$state', 'ProjectResource', 'AnalysisResource'];

  var NetworkMetaAnalysisModelContainerController = function($scope, $state, ProjectResource, AnalysisResource) {

       $scope.project = ProjectResource.get({ projectId: $state.params.projectId});
       $scope.analysis = AnalysisResource.get($state.params);
  };

  return dependencies.concat(NetworkMetaAnalysisModelContainerController);
});
