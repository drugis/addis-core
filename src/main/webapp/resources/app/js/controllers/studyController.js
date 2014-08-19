'use strict';
define([], function() {
  var dependencies = ['$scope', '$q', '$stateParams', 'TrialverseResource', 'StudyDetailsResource',
    'StudyTreatmentActivityResource', 'StudyArmResource', 'StudyEpochResource', 'StudyDesignService'
  ];
  var StudyController = function($scope, $q, $stateParams, TrialverseResource, StudyDetailsResource,
    StudyTreatmentActivityResource, StudyArmResource, StudyEpochResource, StudyDesignService) {
    $scope.namespace = TrialverseResource.get($stateParams);
    $scope.studyDetails = StudyDetailsResource.get($stateParams);
    $scope.studyArms = StudyArmResource.query($stateParams);
    $scope.studyEpochs = StudyEpochResource.query($stateParams);
    $scope.treatmentActivities = StudyTreatmentActivityResource.query($stateParams);

    $scope.epochTreatments = function(epochUid, armUid) {
      return _.filter($scope.treatmentActivities, function(activity) {
        if(epochUid === activity.epochUid && armUid === activity.armUid) {
          return activity;
        }
      });
    };

    // $q.all([
    //   $scope.studyEpochs.$promise,
    //   $scope.studyArms.$promise,
    //   $scope.treatmentActivities.$promise,

    // ])
    //   .then(function() {
    //     $scope.studyDesignTable = StudyDesignService.buildStudyDesignTable(
    //       $scope.studyEpochs, $scope.studyArms, $scope.treatmentActivities);
    //   });

  };
  return dependencies.concat(StudyController);
});