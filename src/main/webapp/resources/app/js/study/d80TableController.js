'use strict';
define(['lodash', 'mctad'], function(_, mctad) {
  var dependencies = ['$scope', '$filter', '$modalInstance', '$q', 'EpochService', 'ArmService',
    'ActivityService', 'StudyDesignService', 'EndpointService', 'MeasurementMomentService', 'ResultsService',
    'EstimatesResource', 'study'
  ];
  var D80TableController = function($scope, $filter, $modalInstance, $q, EpochService, ArmService,
    ActivityService, StudyDesignService, EndpointService, MeasurementMomentService, ResultsService,
    EstimatesResource, study) {
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
          var estimates = EstimatesResource.getEstimates($scope.measurements);

          $scope.effectEstimateRows = [];
          estimates.$promise.then(function(estimateResults) {
            var baseline = _.find($scope.arms, ['armURI', estimateResults.baselineUri]);
            var subjectArms = _.reject($scope.arms, ['armURI', estimateResults.baselineUri]);
            _.forEach(subjectArms, function(arm) {
              _.forEach($scope.endpoints, function(endpoint) {
                var estimate = estimateResults[endpoint.uri][arm.armURI];
                $scope.effectEstimateRows.push({
                  endpoint: endpoint,
                  arm: arm,
                  difference: getDifference(estimate),
                  confidenceInterval: getConfidenceInterval(estimate),
                  pValue: getPValue(estimate)
                });
              });
            });
          });
        });
      }
    });

    function getDifference(baseline, subject) {
      if (baseline.type === 'dichotomous') {
        // var sampleSize = baseline.sampleSize + subject.sampleSize;
        // var degreesOfFreedom = sampleSize - 2;
        // var mu = Math.log((subject.count / subject.sampleSize) / (baseline.count / baseline.sampleSize));
        // var sigma = Math.sqrt((1.0 / subject.count) + (1.0 / baseline.count) - //NB: this is the LOG error
        //   (1.0 / subject.sampleSize) - (1.0 / baseline.sampleSize));
        // var distribution = mctad.t(degreesOfFreedom); // new TransformedLogStudentT(mu, sigma, degreesOfFreedom);
        // var pointEstimate = calculateQuantile(distribution, 0.5, sigma, mu);
      }
    }

    function calculateQuantile(distribution, quantile, sigma, mu) {
      // given we want to use quartiles with a non-sample population
      var quartiles = {};
      if (distribution.size % 2 === 0) {
        // even distro
        quartiles.first = Math.ceil(0.25 * distribution.size);
        quartiles.firstValue = distribution[quartiles.first];
        quartiles.second = 0.5 * distribution.size;
        quartiles.secondValue = (distribution[quartiles.second] + distribution[quartiles.second + 1]) / 2;
        quartiles.third = Math.ceil(0.75 * distribution.size);
        quartiles.thirdValue = distribution[quartiles.first];
      } else {
        quartiles.first = Math.ceil(0.25 * distribution.size);
        quartiles.firstValue = distribution[quartiles.first];
        quartiles.second = Math.ceil(0.5 * distribution.size);
        quartiles.secondValue = distribution[quartiles.second];
        quartiles.third = Math.ceil(0.75 * distribution.size);
        quartiles.thirdValue = distribution[quartiles.first];
      }
      return quartiles;
      // return (1 - distribution.cdf(quantile)) * sigma + mu;
    }

    function getConfidenceInterval(measurement, mu, sigma) {
      //confidence level 99% --> zstar = 2.576
      //confidence level 98% --> zstar = 2.326
      //confidence level 95% --> zstar = 1.96
      //confidence level 90% --> zstar = 1.645
      var zstar = 1.96;
      var n = measurement.sampleSize;
      var lower = mu - zstar * (sigma / (Math.sqrt(n)));
      var upper = mu + zstar * (sigma / (Math.sqrt(n)));
      return {
        lower: lower,
        upper: upper
      };
    }

    function getPValue(measurement) {}

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
