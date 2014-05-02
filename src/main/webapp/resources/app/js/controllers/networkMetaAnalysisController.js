define([], function() {
  var dependencies = ['$scope'
  ];

  var NetworkMetaAnalysisController = function($scope) {
    $scope.analysis = $scope.$parent.analysis;
    $scope.$parent.loading = {
      loaded: true
    };
  };

  return dependencies.concat(NetworkMetaAnalysisController);
});