'use strict';
define([], function() {
  var dependencies = ['$scope', '$q', '$stateParams', 'TrialverseResource', 'StudyDetailsResource',
    'StudyTreatmentActivityResource', 'StudyGroupResource', 'StudyEpochResource',
    'StudyPopulationCharacteristicsResource', 'StudyEndpointsResource', 'StudyAdverseEventsResource',
    'StudyReadOnlyService'
  ];
  var StudyController = function($scope, $q, $stateParams, TrialverseResource, StudyDetailsResource,
    StudyTreatmentActivityResource, StudyGroupResource, StudyEpochResource,
    StudyPopulationCharacteristicsResource, StudyEndpointsResource, StudyAdverseEventsResource, StudyReadOnlyService) {

    $scope.designRows = [];
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
      $scope.studyPopulationCharacteristics = StudyPopulationCharacteristicsResource.get(studyCoordinates);
      $scope.studyEndpoints = StudyEndpointsResource.get(studyCoordinates);
      $scope.studyAdverseEvents = StudyAdverseEventsResource.get(studyCoordinates);

      $scope.studyGroups.$promise.then(function(groups) {
        $scope.numberOfArms = groups.reduce(function(accum, group) {
          if (group.isArm === 'true') { // i will go to hell for this
            accum = accum + 1;
          }
          return accum;
        }, 0);

        $scope.numberOfNonArmGroups = groups.length - $scope.numberOfArms;
      });

      $q.all([$scope.studyPopulationCharacteristics.$promise, $scope.studyGroups.$promise]).then(function(results) {
        var popChars = results[0];
        var groups = results[1];
        $scope.populationCharacteristicsRows = StudyReadOnlyService.flattenOutcomesToTableRows(popChars, groups);
      });

      $q.all([$scope.studyEndpoints.$promise, $scope.studyGroups.$promise]).then(function(results) {
        var endPoints = results[0];
        var groups = results[1];
        $scope.endpointRows = StudyReadOnlyService.flattenOutcomesToTableRows(endPoints, groups);
      });

      $q.all([$scope.studyAdverseEvents.$promise, $scope.studyGroups.$promise]).then(function(results) {
        var adverseEvents = results[0];
        var groups = results[1];
        $scope.adverseEventsRows = StudyReadOnlyService.flattenOutcomesToTableRows(adverseEvents, groups);
      });

      $q.all([$scope.studyGroups.$promise, $scope.studyEpochs.$promise, $scope.treatmentActivities.$promise])
        .then(function(results) {
          $scope.designRows = $scope.designRows.concat(StudyReadOnlyService.constructStudyDesignTableRows(results[0], results[1], results[2]));
        });
    });

  };
  return dependencies.concat(StudyController);
});
