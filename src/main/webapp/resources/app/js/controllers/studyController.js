'use strict';
define([], function() {
  var dependencies = ['$scope', '$stateParams', 'TrialverseResource', 'StudyDetailsResource',
    'StudyTreatmentActivityResource', 'StudyArmResource', 'StudyEpochResource',
    'StudyPopulationCharacteristicsResource', 'StudyEndpointsResource', 'StudyAdverseEventsResource'
  ];
  var StudyController = function($scope, $stateParams, TrialverseResource, StudyDetailsResource,
    StudyTreatmentActivityResource, StudyArmResource, StudyEpochResource,
    StudyPopulationCharacteristicsResource, StudyEndpointsResource, StudyAdverseEventsResource) {

    $scope.namespace = TrialverseResource.get($stateParams);
    $scope.studyDetails = StudyDetailsResource.get($stateParams);

    $scope.studyArms = StudyArmResource.query($stateParams);
    $scope.studyEpochs = StudyEpochResource.query($stateParams);
    $scope.treatmentActivities = StudyTreatmentActivityResource.query($stateParams);

    $scope.studyPopulationCharacteristics = StudyPopulationCharacteristicsResource.get($stateParams);
    $scope.studyEndpoints = StudyEndpointsResource.get($stateParams);
    $scope.studyAdverseEvents = StudyAdverseEventsResource.get($stateParams);

    $scope.cellTreatments = function(epochUid, armUid) {
      return _.filter($scope.treatmentActivities, function(activity) {
        var cellHasApplication = _.find(activity.activityApplications, function(application) {
          return (application.epochUid === epochUid && application.armUid === armUid);
        });
        if (cellHasApplication) {
          return activity;
        }
      });
    };

    $scope.flattenMoments = function(outcomes) {
      _.each(outcomes, function(outcome) {
        outcome.momentAndValues = [];
        _.each(outcome.studyDataMoments, function(moment) {
          outcome.relativeToAnchorOntology = moment.relativeToAnchorOntology;
          outcome.relativeToEpochLabel = moment.relativeToEpochLabel;
          outcome.timeOffsetDuration = moment.timeOffsetDuration;
          outcome.momentAndValues = outcome.momentAndValues.concat(moment.studyDataArmValues);
        });
      });
      return outcomes;
    };


  };
  return dependencies.concat(StudyController);
});