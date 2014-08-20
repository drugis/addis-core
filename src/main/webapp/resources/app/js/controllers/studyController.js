'use strict';
define([], function() {
  var dependencies = ['$scope', '$q', '$stateParams', 'TrialverseResource', 'StudyDetailsResource',
    'StudyTreatmentActivityResource', 'StudyArmResource', 'StudyEpochResource'
  ];
  var StudyController = function($scope, $q, $stateParams, TrialverseResource, StudyDetailsResource,
    StudyTreatmentActivityResource, StudyArmResource, StudyEpochResource) {
    $scope.namespace = TrialverseResource.get($stateParams);
    $scope.studyDetails = StudyDetailsResource.get($stateParams);
    $scope.studyArms = StudyArmResource.query($stateParams);
    $scope.studyEpochs = StudyEpochResource.query($stateParams);
    $scope.treatmentActivities = StudyTreatmentActivityResource.query($stateParams);

    $scope.cellTreatments = function(epochUid, armUid) {
      return _.filter($scope.treatmentActivities, function(activity) {
        var cellHasApplication = _.find(activity.activityApplications, function(application) {
          return (application.epochUid === epochUid && application.armUid === armUid)
        });
        if (cellHasApplication) {
          return activity;
        }
      });
    };


  };
  return dependencies.concat(StudyController);
});