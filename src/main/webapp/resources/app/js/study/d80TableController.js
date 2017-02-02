'use strict';
define(['lodash', 'clipboard'], function(_, Clipboard) {
  var dependencies = ['$scope', '$filter', '$modalInstance', '$q', 'EpochService', 'ArmService',
    'ActivityService', 'StudyDesignService', 'EndpointService', 'MeasurementMomentService', 'ResultsService',
    'EstimatesResource', 'D80TableService', 'study'
  ];
  var D80TableController = function($scope, $filter, $modalInstance, $q, EpochService, ArmService,
    ActivityService, StudyDesignService, EndpointService, MeasurementMomentService, ResultsService,
    EstimatesResource, D80TableService, study) {
    $scope.study = study;
    $scope.buildResultLabel = D80TableService.buildResultLabel;

    var clipboard = new Clipboard('.clipboard-button');
    var exponentialFilter = $filter('exponentialFilter'),
      durationFilter = $filter('durationFilter');

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
          arm.treatmentLabel = arm.activity.treatments.length === 0 ? '<treatment>' : buildArmTreatmentsLabel(arm.activity.treatments);
          return arm;
        });
        var primaryMeasurementMoment = _.find($scope.measurementMoments, function(measurementMoment) {
          return measurementMoment.offset === 'PT0S' && measurementMoment.relativeToAnchor === 'ontology:anchorEpochEnd' &&
            measurementMoment.epochUri === $scope.primaryEpoch.uri;
        });
        var resultsPromises = _.map(_.map($scope.endpoints, 'uri'), ResultsService.queryResultsByOutcome);
        $q.all(resultsPromises).then(function(results) {
          var endpointsByUri = _.keyBy($scope.endpoints, 'uri');

          var resultsByEndpointAndArm = D80TableService.buildResultsByEndpointAndArm(results, primaryMeasurementMoment.uri);

          var toBackEndMeasurements = [];
          $scope.measurements = _.reduce(resultsByEndpointAndArm, function(accum, endPointResultsByArm, endpointUri) {
            accum[endpointUri] = _.reduce(endPointResultsByArm, function(accum, armResults, armUri) {
              var resultsObject = D80TableService.buildResultsObject(armResults, endpointsByUri[endpointUri], armUri);
              resultsObject.label = D80TableService.buildResultLabel(resultsObject);
              toBackEndMeasurements.push(resultsObject);
              accum[armUri] = resultsObject;
              return accum;
            }, {});
            return accum;
          }, {});

          var estimates = EstimatesResource.getEstimates({
            measurements: toBackEndMeasurements
          });

          estimates.$promise.then(function(estimateResults) {
            $scope.effectEstimateRows = D80TableService.buildEstimateRows(estimateResults, $scope.endpoints, $scope.arms);
          });
        });
      }
    });


    function buildArmTreatmentsLabel(treatments) {
      var treatmentLabels = _.map(treatments, function(treatment) {
        if (treatment.treatmentDoseType === 'ontology:FixedDoseDrugTreatment') {
          return treatment.drug.label + ' ' + exponentialFilter(treatment.fixedValue) +
            ' ' + treatment.doseUnit.label + ' per ' + durationFilter(treatment.dosingPeriodicity);
        } else if (treatment.treatmentDoseType === 'ontology:TitratedDoseDrugTreatment') {
          return treatment.drug.label + ' ' + exponentialFilter(treatment.minValue) +
            '-' + exponentialFilter(treatment.minValue) + ' ' + treatment.doseUnit.label + ' per ' +
            durationFilter(treatment.dosingPeriodicity);
        } else {
          throw ('unknown dosage type');
        }
      });
      return treatmentLabels.join(' + ');
    }

    function queryItems(service, scopeProperty) {
      return service.queryItems().then(function(resolvedValue) {
        $scope[scopeProperty] = resolvedValue;
        return resolvedValue;
      });
    }

    $scope.cancel = function() {
      $modalInstance.dismiss('cancel');
    };
  };
  return dependencies.concat(D80TableController);
});
