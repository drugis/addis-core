'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$q', '$stateParams', 'TrialverseResource', 'StudyDetailsResource',
    'StudyTreatmentActivityResource', 'StudyGroupResource', 'StudyEpochResource',
    'StudyPopulationCharacteristicsResource', 'StudyEndpointsResource', 'StudyAdverseEventsResource'
  ];
  var StudyController = function($scope, $q, $stateParams, TrialverseResource, StudyDetailsResource,
    StudyTreatmentActivityResource, StudyGroupResource, StudyEpochResource,
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
      $scope.studyGroups = StudyGroupResource.query(studyCoordinates);
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
      $q.all([$scope.studyGroups.$promise, $scope.studyEpochs.$promise, $scope.treatmentActivities.$promise])
        .then(function() {
          $scope.designRows = $scope.designRows.concat(constructStudyDesignTableRows());
        });
    });
    $scope.designRows = [];


    function constructStudyDesignTableRows() {
      var rows = [];
      var arms  = $scope.studyGroups.filter(function (group){
        return group.isArm === 'true'; //evil, moe haha ha
       });
      _.each(arms, function(group) {
        var row = {};
        row.label = group.label;
        row.numberOfParticipantsStarting = group.numberOfParticipantsStarting;
        row.epochCells = [];

        _.each($scope.studyEpochs, function(epoch) {
          row.epochCells.push(getCellTreatments(epoch.epochUid, group.groupUri)[0]);
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
          row.studyDataValues = moment.studyDataValues;
          row.studyDataValues.sort(function(a, b) {
            return a.label.localeCompare(b.label);
          });
          rows.push(row);
        });

      });
      return rows;
    }

    function getCellTreatments(epochUid, groupUri) {
      return _.filter($scope.treatmentActivities, function(activity) {
        var cellHasApplication = _.find(activity.activityApplications, function(application) {
          return (application.epochUid === epochUid && application.armUid === groupUri);
        });
        if (cellHasApplication) {
          return activity;
        }
      });
    }


  };
  return dependencies.concat(StudyController);
});
