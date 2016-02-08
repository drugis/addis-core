'use strict';
define([], function() {
  var dependencies = ['$scope', '$q', '$stateParams', 'TrialverseResource', 'StudyDetailsResource',
    'StudyTreatmentActivityResource', 'StudyArmResource', 'StudyEpochResource',
    'StudyPopulationCharacteristicsResource', 'StudyEndpointsResource', 'StudyAdverseEventsResource'
  ];
  var StudyController = function($scope, $q, $stateParams, TrialverseResource, StudyDetailsResource,
    StudyTreatmentActivityResource, StudyArmResource, StudyEpochResource,
    StudyPopulationCharacteristicsResource, StudyEndpointsResource, StudyAdverseEventsResource) {

    $scope.project.$promise.then(function() {
      var studyCoordinates = {
        namespaceUid: $scope.project.namespaceUid,
        studyUid: $stateParams.studyUid
      };
      $scope.namespace = TrialverseResource.get({
        namespaceUid: $scope.project.namespaceUid
      });
      $scope.studyDetails = StudyDetailsResource.get(studyCoordinates);
      $scope.studyArms = StudyArmResource.query(studyCoordinates);
      $scope.studyEpochs = StudyEpochResource.query(studyCoordinates);
      $scope.treatmentActivities = StudyTreatmentActivityResource.query(studyCoordinates);
      $scope.studyPopulationCharacteristics = StudyPopulationCharacteristicsResource
        .get(studyCoordinates, function(populationCharacteristics) {
          $scope.populationCharacteristicsRows = flattenOutcomesToTableRows(populationCharacteristics);
        });
      $scope.studyEndpoints = StudyEndpointsResource.get(studyCoordinates, function(endpoints) {
        $scope.endpointRows = flattenOutcomesToTableRows(endpoints);
      });
      $scope.studyAdverseEvents = StudyAdverseEventsResource.get(studyCoordinates, function(adverseEvents) {
        $scope.adverseEventsRows = flattenOutcomesToTableRows(adverseEvents);
      });
      $q.all([$scope.studyArms.$promise, $scope.studyEpochs.$promise, $scope.treatmentActivities.$promise])
        .then(function() {
          $scope.designRows = $scope.designRows.concat(constructStudyDesignTableRows());
        });
    });
    $scope.designRows = [];


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
          row.studyDataArmValues.sort(function(a, b) {
            return a.armLabel.localeCompare(b.armLabel);
          });
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
