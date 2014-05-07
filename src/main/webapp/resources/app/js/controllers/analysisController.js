define([], function() {
  var dependencies = ['$scope', '$state', '$q', 'currentAnalysis', 'currentProject', 'ANALYSIS_TYPES']
  var AnalysisController = function($scope, $state, $q, currentAnalysis, currentProject, ANALYSIS_TYPES) {
    $scope.analysis = currentAnalysis;
    $scope.project = currentProject;
    $scope.loading = {
      loaded: false
    };
    currentAnalysis.$promise.then(function(analysis) {
      var analysisType = _.find(ANALYSIS_TYPES, function(type) {
        return type.label === analysis.analysisType;
      });
      $state.go(analysisType.stateName, {
        type: analysis.analysisType,
        analysisId: analysis.id
      });
    });
    $q.all(currentAnalysis, currentProject).then(function() {
      $scope.loading.loaded = true;
    });
  }

  return dependencies.concat(AnalysisController);
});