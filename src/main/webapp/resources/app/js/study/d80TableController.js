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
    $scope.cancel = cancel;

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
      $scope.baseline = $scope.arms[0];
      $scope.selected = {
        measurementMoment: getInitialMeasurementMoment()
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
        $scope.measurements = buildMeasurements(results);
        buildEstimateRows();
      });
    }

    function buildMeasurements(results) {
      return D80TableService.buildMeasurements(results, $scope.selected.measurementMoment.uri, $scope.endpoints);
    }

    function getInitialMeasurementMoment() {
      var primaryMeasurementMoment;
      if ($scope.primaryEpoch) {
        primaryMeasurementMoment = _.find($scope.measurementMoments, function(measurementMoment) {
          return measurementMoment.offset === 'PT0S' && measurementMoment.relativeToAnchor === 'ontology:anchorEpochEnd' &&
            measurementMoment.epochUri === $scope.primaryEpoch.uri;
        });
      }
      return primaryMeasurementMoment && $scope.measurementMoments ? primaryMeasurementMoment : $scope.measurementMoments[0];
    }

    function createArms() {
      return _.map($scope.arms, function(arm) {
        arm.activity = getActivityForArm(arm);
        arm.treatmentLabel = arm.activity.treatments.length === 0 ? '<treatment>' : D80TableService.buildArmTreatmentsLabel(arm.activity.treatments);
        return arm;
      });
    }

    function getActivityForArm(arm) {
      var activityUri = getActivityUri(arm);
      return _.find($scope.activities, function(activity) {
        return activity.activityUri === activityUri;
      });
    }

    function getActivityUri(arm) {
      return _.find($scope.designCoordinates, function(coordinate) {
        return coordinate.armUri === arm.armURI && coordinate.epochUri === $scope.primaryEpoch.uri;
      }).activityUri;
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
        baselineUri: $scope.baseline.armURI
      }).$promise.then(function(estimateResults) {
        $scope.effectEstimateRows = D80TableService.buildEstimateRows(estimateResults, $scope.endpoints, $scope.arms);
      });
    }

    function cancel() {
      $modalInstance.close();
    }
  };
  return dependencies.concat(D80TableController);
});
