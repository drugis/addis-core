'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', 'TrialverseResource', 'TrialverseStudiesWithDetailsResource'];
  var NamespaceController = function($scope, $stateParams, TrialverseResource, TrialverseStudiesWithDetailsResource) {
    $scope.namespace = TrialverseResource.get($stateParams);
    $scope.studiesWithDetails = TrialverseStudiesWithDetailsResource.get($stateParams);
    $scope.reverse = false;
    $scope.orderBypredicate = 'study.name';
  };
  return dependencies.concat(NamespaceController);
});