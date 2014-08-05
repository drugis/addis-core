'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', 'TrialverseResource','TrialverseStudyWithDetailsResource'];
  var NamespaceController = function($scope, $stateParams, TrialverseResource, TrialverseStudyWithDetailsResource) {
  	$scope.namespace = TrialverseResource.get($stateParams);
    $scope.studies = TrialverseStudyWithDetailsResource.get($stateParams);
  };
  return dependencies.concat(NamespaceController);
});