'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', 'TrialverseResource','StudyDetailsResource'];
  var StudyController = function($scope, $stateParams, TrialverseResource, StudyDetailsResource) {
    $scope.namespace = TrialverseResource.get($stateParams);
    $scope.studyDetails = StudyDetailsResource.get($stateParams);
  };
  return dependencies.concat(StudyController);
});