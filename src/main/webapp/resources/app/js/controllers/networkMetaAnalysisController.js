define([], function() {
  var dependencies = ['$scope'
  ];

  var NetworkMetaAnalysisController = function($scope) {
     $scope.analysis = $scope.$parent.analysis;
    $scope.$parent.loading = {
      loaded: false
    };
  };

  return dependencies.concat(NetworkMetaAnalysisController);
});