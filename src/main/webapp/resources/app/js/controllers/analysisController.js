'use strict';
define([], function() {
  var dependencies = ['$scope', '$state', '$q', '$window', 'currentAnalysis', 'currentProject', 'ANALYSIS_TYPES'];
  var AnalysisController = function($scope, $state, $q, $window, currentAnalysis, currentProject, ANALYSIS_TYPES) {

    $scope.loading = {loaded : true};

    // for addis use
    $scope.analysis = currentAnalysis;

    // for mcda use
    $scope.workspace = $scope.analysis;
    $scope.project = currentProject;

    $scope.isProblemDefined = false;

    var analysisType = _.find(ANALYSIS_TYPES, function(type) {
      return type.label === currentAnalysis.analysisType;
    });

    if (currentAnalysis.problem) {
      $scope.isProblemDefined = true;
    }
    $scope.editMode = {
      isUserOwner: $window.config.user.id === currentProject.owner.id,
    };
    $scope.editMode.disableEditing = !$scope.editMode.isUserOwner || $scope.isProblemDefined;
  };

  return dependencies.concat(AnalysisController);
});
