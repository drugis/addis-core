define([], function() {
  var dependencies = ['$scope', '$state', '$q', '$window', 'currentAnalysis', 'currentProject', 'ANALYSIS_TYPES']
  var AnalysisController = function($scope, $state, $q, $window, currentAnalysis, currentProject, ANALYSIS_TYPES) {
    $scope.analysis = currentAnalysis;
    $scope.project = currentProject;
    $scope.loading = {
      loaded: false
    };
    $scope.editMode = {
      disableEditing: true
    }
    $scope.isProblemDefined = false;

    currentAnalysis.$promise.then(function(analysis) {
      var analysisType = _.find(ANALYSIS_TYPES, function(type) {
        return type.label === analysis.analysisType;
      });

      if ($state.current && $state.current.name === 'analysis') {
        $state.go(analysisType.stateName, {
          type: analysis.analysisType,
          analysisId: analysis.id
        });
      }

    });
    $q.all([currentAnalysis.$promise, currentProject.$promise]).then(function() {
      if (currentAnalysis.problem) {
        $scope.isProblemDefined = true;
      }
      var userIsOwner = $window.config.user.id === currentProject.owner.id;
      $scope.editMode.disableEditing = !userIsOwner || $scope.isProblemDefined;
      $scope.loading.loaded = true;
    });
  }

  return dependencies.concat(AnalysisController);
});