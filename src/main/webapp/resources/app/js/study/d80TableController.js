'use strict';
define(['lodash', 'clipboard'], function(_, Clipboard) {
  var dependencies = ['$scope', '$filter', '$modalInstance', '$q', 'EpochService', 'ArmService',
    'ActivityService', 'StudyDesignService', 'EndpointService', 'MeasurementMomentService', 'ResultsService',
    'EstimatesResource', 'D80TableService', 'study'
  ];
  var D80TableController = function($scope, $filter, $modalInstance, $q, EpochService, ArmService,
    ActivityService, StudyDesignService, EndpointService, MeasurementMomentService, ResultsService,
    EstimatesResource, D80TableService, study) {
    // functions
    $scope.selectBaseLine = selectBaseLine;
    $scope.cancel = cancel;

    // init
    $scope.study = study;
    $scope.buildResultLabel = D80TableService.buildResultLabel;

    var clipboard = new Clipboard('.clipboard-button');

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
      if ($scope.primaryEpoch) {
        $scope.arms = _.map($scope.arms, function(arm) {
          var coord = _.find($scope.designCoordinates, function(coordinate) {
            return coordinate.armUri === arm.armURI && coordinate.epochUri === $scope.primaryEpoch.uri;
          });
          arm.activity = _.find($scope.activities, function(activity) {
            return activity.activityUri === coord.activityUri;
          });
          arm.treatmentLabel = arm.activity.treatments.length === 0 ? '<treatment>' : D80TableService.buildArmTreatmentsLabel(arm.activity.treatments);
          return arm;
        });
        $scope.baseline = $scope.arms[0];
        var primaryMeasurementMoment = _.find($scope.measurementMoments, function(measurementMoment) {
          return measurementMoment.offset === 'PT0S' && measurementMoment.relativeToAnchor === 'ontology:anchorEpochEnd' &&
            measurementMoment.epochUri === $scope.primaryEpoch.uri;
        });
        var resultsPromises = _.map(_.map($scope.endpoints, 'uri'), ResultsService.queryResultsByOutcome);
        $q.all(resultsPromises).then(function(results) {
          $scope.measurements = D80TableService.buildMeasurements(results, primaryMeasurementMoment.uri, $scope.endpoints);

          var estimates = EstimatesResource.getEstimates({
            measurements: $scope.measurements.toBackEndMeasurements,
            baselineUri: $scope.baseline.armURI
          });

          estimates.$promise.then(function(estimateResults) {
            $scope.effectEstimateRows = D80TableService.buildEstimateRows(estimateResults, $scope.endpoints, $scope.arms);
          });
        });
      }
    });

    function queryItems(service, scopeProperty) {
      return service.queryItems().then(function(resolvedValue) {
        $scope[scopeProperty] = resolvedValue;
        return resolvedValue;
      });
    }

    function selectBaseLine(newBaseline) {
      $scope.baseline = newBaseline;
      var estimates = EstimatesResource.getEstimates({
        measurements: $scope.measurements.toBackEndMeasurements,
        baselineUri: $scope.baseline.armURI
      });

      estimates.$promise.then(function(estimateResults) {
        $scope.effectEstimateRows = D80TableService.buildEstimateRows(estimateResults, $scope.endpoints, $scope.arms);
      });
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }
  };
  return dependencies.concat(D80TableController);
});