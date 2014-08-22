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

    $scope.studyPopulationCharacteristics = StudyPopulationCharacteristicsResource
      .get($stateParams, function(populationCharacteristics){
        $scope.populationCharacteristicsRows = flattenOutcomesToTableRows(populationCharacteristics);
      });

    $scope.studyEndpoints = StudyEndpointsResource.get($stateParams, function(endpoints) {
      $scope.endpointRows = flattenOutcomesToTableRows(endpoints);
    });

    $scope.studyAdverseEvents = StudyAdverseEventsResource.get($stateParams, function(adverseEvents) {
      $scope.adverseEventsRows = flattenOutcomesToTableRows(adverseEvents);
    });

    function flattenOutcomesToTableRows (outcomes) {
      var rows = [];
      _.each(outcomes, function(outcome) {
        _.each(outcome.studyDataMoments, function(moment) {
          var row = {};
          row.studyDataTypeLabel = outcome.studyDataTypeLabel;
          row.relativeToAnchorOntology = moment.relativeToAnchorOntology;
          row.relativeToEpochLabel = moment.relativeToEpochLabel;
          row.timeOffsetDuration = moment.timeOffsetDuration;
          row.studyDataArmValues = moment.studyDataArmValues;
          rows.push(row);
        });

      });
      return rows;
    }

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


  };
  return dependencies.concat(StudyController);
});