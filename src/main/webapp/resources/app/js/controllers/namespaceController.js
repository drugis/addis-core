'use strict';
define([], function() {
  var dependencies = ['$scope', 'TrialverseResource'];
  var NamespaceController = function($scope, TrialverseResource) {
    $scope.namespace = TrialverseResource.get($stateParams);
  };
  return dependencies.concat(NamespaceController);
});