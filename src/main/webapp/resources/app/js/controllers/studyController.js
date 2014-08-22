'use strict';
define([], function() {
  var dependencies = ['$scope', '$q', '$stateParams', 'TrialverseResource', 'StudyDetailsResource',
    'StudyTreatmentActivityResource', 'StudyArmResource', 'StudyEpochResource',
    'StudyPopulationCharacteristicsResource', 'StudyEndpointsResource', 'StudyAdverseEventsResource'
  ];
  var StudyController = function($scope, $q, $stateParams, TrialverseResource, StudyDetailsResource,
    StudyTreatmentActivityResource, StudyArmResource, StudyEpochResource,
    StudyPopulationCharacteristicsResource, StudyEndpointsResource, StudyAdverseEventsResource) {

    $scope.namespace = TrialverseResource.get($stateParams);
    $scope.studyDetails = StudyDetailsResource.get($stateParams);
    $scope.studyArms = StudyArmResource.query($stateParams);
    $scope.studyEpochs = StudyEpochResource.query($stateParams);
    $scope.treatmentActivities = StudyTreatmentActivityResource.query($stateParams);
    $scope.studyPopulationCharacteristics = StudyPopulationCharacteristicsResource
      .get($stateParams, function(populationCharacteristics) {
        $scope.populationCharacteristicsRows = flattenOutcomesToTableRows(populationCharacteristics);
      });
    $scope.studyEndpoints = StudyEndpointsResource.get($stateParams, function(endpoints) {
      $scope.endpointRows = flattenOutcomesToTableRows(endpoints);
    });
    $scope.studyAdverseEvents = StudyAdverseEventsResource.get($stateParams, function(adverseEvents) {
      $scope.adverseEventsRows = flattenOutcomesToTableRows(adverseEvents);
    });
    $scope.designRows = [];

    $q.all([$scope.studyArms.$promise, $scope.studyEpochs.$promise, $scope.treatmentActivities.$promise])
      .then(function() {
        $scope.designRows = $scope.designRows.concat(constructStudyDesignTableRows());
      });

    function constructStudyDesignTableRows() {
      var rows = [];
      _.each($scope.studyArms, function(arm) {
        var row = {};
        row.armLabel = arm.armLabel;
        row.numberOfParticipantsStarting = arm.numberOfParticipantsStarting;
        row.epochCells = [];

        _.each($scope.studyEpochs, function(epoch) {
          row.epochCells.push(getCellTreatments(epoch.epochUid, arm.armUid)[0]);
        });
        rows.push(row);
      });
      return rows;
    }

    function flattenOutcomesToTableRows(outcomes) {
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

    function getCellTreatments(epochUid, armUid) {
      return _.filter($scope.treatmentActivities, function(activity) {
        var cellHasApplication = _.find(activity.activityApplications, function(application) {
          return (application.epochUid === epochUid && application.armUid === armUid);
        });
        if (cellHasApplication) {
          return activity;
        }
      });
    }


  };
  return dependencies.concat(StudyController);
});