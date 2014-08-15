'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', 'TrialverseResource', 'StudyDetailsResource',
    'StudyDesignResource', 'StudyArmResource', 'StudyDesignService'
  ];
  var StudyController = function($scope, $stateParams, TrialverseResource, StudyDetailsResource,
    StudyDesignResource, StudyArmResource, StudyDesignService) {
    $scope.namespace = TrialverseResource.get($stateParams);
    $scope.studyDetails = StudyDetailsResource.get($stateParams);
    $scope.studyArms = StudyArmResource.query($stateParams);
    StudyDesignResource.get($stateParams, function(treatmentActivities) {
      $scope.studyDesignTable = StudyDesignService.buildStudyDesignTable(treatmentActivities);
    });
  };
  return dependencies.concat(StudyController);
});