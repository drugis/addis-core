'use strict';
define(['lodash', 'clipboard'], function(_, Clipboard) {
  var dependencies = [
    '$scope',
    '$modalInstance',
    '$q',
    'EpochService',
    'ArmService',
    'ActivityService',
    'StudyDesignService',
    'EndpointService',
    'MeasurementMomentService',
    'ResultsService',
    'EstimatesResource',
    'D80TableService',
    'study'
  ];
  var D80TableController = function(
    $scope,
    $modalInstance,
    $q,
    EpochService,
    ArmService,
    ActivityService,
    StudyDesignService,
    EndpointService,
    MeasurementMomentService,
    ResultsService,
    EstimatesResource,
    D80TableService,
    study
  ) {
    // functions
    $scope.buildEstimateRows = buildEstimateRows;
    $scope.buildTable = buildTable;
    $scope.cancel = $modalInstance.close();

    // init
    $scope.study = study;
    $scope.selected = {};

    new Clipboard('.clipboard-button');

    var allThePromises = [
      queryItems(EpochService, 'epochs').then(function(epochs) {
        $scope.primaryEpoch = _.find(epochs, 'isPrimary');
      }),
      queryItems(ArmService, 'arms'),
      queryItems(ActivityService, 'activities'),
      queryItems(StudyDesignService, 'designCoordinates'),
      queryItems(EndpointService, 'endpoints'),
      queryItems(MeasurementMomentService, 'measurementMoments')
    ];

    $q.all(allThePromises).then(function() {
      $scope.arms = createArms();
      $scope.selected = {
        baseline:  $scope.arms[0],
        measurementMoment: D80TableService.getInitialMeasurementMoment($scope.measurementMoments, $scope.primaryEpoch)
      };

      if (!$scope.selected.measurementMoment) {
        return;
      } else {
        $scope.resultsPromises = _.map(_.map($scope.endpoints, 'uri'), ResultsService.queryResultsByOutcome);
        buildTable();
      }
    });

    function buildTable() {
      $q.all($scope.resultsPromises).then(function(results) {
        $scope.measurements = D80TableService.buildMeasurements(
          results, $scope.selected.measurementMoment.uri, $scope.endpoints
        );
        buildEstimateRows();
      });
    }

    function createArms() {
      return _.map($scope.arms, function(arm) {
        if ($scope.primaryEpoch) {
          arm.activity = D80TableService.getActivityForArm(arm, $scope.activities, $scope.designCoordinates, $scope.primaryEpoch);
          arm.treatmentLabel = arm.activity.treatments.length === 0 ? '<treatment>' : D80TableService.buildArmTreatmentsLabel(arm.activity.treatments);
        }
        return arm;
      });
    }

    function queryItems(service, scopeProperty) {
      return service.queryItems().then(function(resolvedValue) {
        $scope[scopeProperty] = resolvedValue;
        return resolvedValue;
      });
    }

    function buildEstimateRows() {
      EstimatesResource.getEstimates({
        measurements: $scope.measurements.toBackEndMeasurements,
        baselineUri: $scope.selected.baseline.armURI
      }).$promise.then(function(estimateResults) {
        $scope.effectEstimateRows = D80TableService.buildEstimateRows(estimateResults, $scope.endpoints, $scope.arms);
      });
    }
  };
  return dependencies.concat(D80TableController);
});
