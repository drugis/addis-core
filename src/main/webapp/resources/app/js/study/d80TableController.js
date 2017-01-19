'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$scope', '$filter', '$modalInstance', '$q', 'EpochService', 'ArmService',
    'ActivityService', 'StudyDesignService', 'EndpointService', 'MeasurementMomentService', 'ResultsService',
    'study'
  ];
  var D80TableController = function($scope, $filter, $modalInstance, $q, EpochService, ArmService,
    ActivityService, StudyDesignService, EndpointService, MeasurementMomentService, ResultsService, study) {
    $scope.study = study;
    $scope.buildResultLabel = buildResultLabel;

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

          var resultsByEndpointAndArm = {};
          _.forEach(results, function(resultsForOutcome) {
            resultsByEndpointAndArm = _.chain(resultsForOutcome)
              .filter(['momentUri', primaryMeasurementMoment.uri])
              .reduce(function(accum, result) {
                if (!accum[result.outcomeUri]) {
                  accum[result.outcomeUri] = {};
                }
                if (!accum[result.outcomeUri][result.armUri]) {
                  accum[result.outcomeUri][result.armUri] = [];
                }
                accum[result.outcomeUri][result.armUri].push(result);
                return accum;
              }, resultsByEndpointAndArm)
              .value();
          });
          $scope.measurements = _.reduce(resultsByEndpointAndArm, function(accum, endPointResultsByArm, endpointUri) {
            accum[endpointUri] = _.reduce(endPointResultsByArm, function(accum, armResults, armUri) {
              accum[armUri] = buildResultsObject(armResults, endpointsByUri[endpointUri]);
              return accum;
            }, {});
            return accum;
          }, {});
          $scope.effectEstimateRows = [];
          _.foreach($scope.arms.slice(1), function(arm) {
            _.forEach($scope.endpoints, function(endpoint) {
              var measurement = $scope.measurements[endpoint.uri][arm.armURI];
              var baseline = $scope.arms[0];
              $scope.effectEstimateRows.push({
                endpoint: endpoint,
                arm: arm,
                difference: buildDifference(measurement),
                confidenceInterval: buildConfidenceInterval(measurement),
                pValue: buildPValue(measurement)
              });
            });
          });
        });
      }
    });

    function buildDifference(baseline, subject) {
      if(baseline.type === 'dichotomous') {
        var sampleSize = baseline.sampleSize + subject.sampleSize;
        var degreesOfFreedom = sampleSize - 2;
        var mu = Math.log((subject.count / subject.sampleSize) / (baseline.count / baseline.sampleSize));
        var sigma = Math.sqrt((1.0 / subject.count) + (1.0 / baseline.count) - 
                              (1.0 / subject.sampleSize) - (1.0 / baseline.sampleSize));
        var distribution = new TransformedLogStudentT(mu, sigma, degreesOfFreedom);
      }
    }

    function buildConfidenceInterval(measurement) {}

    function buildPValue(measurement) {}

    function findValue(results, property) {
      return _.find(results, ['result_property', property]);
    }

    function buildResultsObject(armResults, endpoint) {
      if (endpoint.measurementType === 'ontology:dichotomous') {
        return {
          type: 'dichotomous',
          count: findValue(armResults, 'count').value,
          sampleSize: findValue(armResults, 'sample_size').value
        };
      } else if (endpoint.measurementType === 'ontology:continuous') {
        return {
          type: 'continuous',
          mean: findValue(armResults, 'mean').value,
          stdDev: findValue(armResults, 'standard_deviation').value,
          sampleSize: findValue(armResults, 'sample_size').value
        };
      }

    }

    function buildResultLabel(resultsObject) {
      if (resultsObject.type === 'dichotomous') {
        return resultsObject.count + '/' + resultsObject.sampleSize;
      } else if (resultsObject.type === 'continuous') {
        return exponentialFilter(resultsObject.mean) + ' ± ' + exponentialFilter(resultsObject.stdDev) +
          ' (' + resultsObject.sampleSize + ')';
      } else {
        throw ('unknown measurement type');
      }

    }

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
