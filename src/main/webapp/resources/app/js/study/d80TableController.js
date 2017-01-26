'use strict';
define(['lodash'], function(_) {
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

          var toBackEndMeasurements = _.reduce(resultsByEndpointAndArm, function(accum, endPointResultsByArm, endpointUri) {
            return accum.concat(_.map(endPointResultsByArm, function(armResults, armUri) {
              return buildResultsObject(armResults, endpointsByUri[endpointUri], endpointUri, armUri);
            }));
          }, []);

          var estimates = EstimatesResource.getEstimates({
            measurements: toBackEndMeasurements
          });

          $scope.effectEstimateRows = [];
          estimates.$promise.then(function(estimateResults) {
            // var baseline = _.find($scope.arms, ['armURI', estimateResults.baselineUri]);
            var subjectArms = _.reject($scope.arms, ['armURI', estimateResults.baselineUri]);
            _.forEach($scope.endpoints, function(endpoint) {
              var mmType = endpoint.measurementType === 'ontology:dichotomous' ? 'Risk ratio' : 'Mean difference';

              var row1 = {
                endpoint: endpoint,
                rowLabel: 'Comparison Groups',
                rowValues: []
              };
              var row2 = {
                rowLabel: mmType,
                rowValues: []
              };
              var row3 = {
                rowLabel: 'Confidence Interval',
                rowValues: []
              };
              var row4 = {
                rowLabel: 'P-value',
                rowValues: []
              };
              _.forEach(subjectArms, function(arm) {
                var estimate = _.find(estimateResults.estimates[endpoint.uri], {
                  'armUri': arm.armURI
                });
                if (estimate) {

                  row1.rowValues.push({
                    value: arm.label
                  });
                  row2.rowValues.push({
                    value: estimate.pointEstimate.toFixed(2)
                  });
                  row3.rowValues.push({
                    value: '(' + estimate.confidenceIntervalLowerBound.toFixed(2) + ', ' + estimate.confidenceIntervalUpperBound.toFixed(2) + ')'
                  });
                  row4.rowValues.push({
                    value: estimate.pValue.toFixed(2)
                  });
                } else {
                  row1.rowValues.push({
                    value: arm.label ? arm.label : '<group descriptors>'
                  });
                  row2.rowValues.push({
                    value: '<point estimate>'
                  });
                  row3.rowValues.push({
                    value: '<confidence interval>'
                  });
                  row4.rowValues.push({
                    value: '<P-value>'
                  });
                }
              });
              $scope.effectEstimateRows.push(row1);
              $scope.effectEstimateRows.push(row2);
              $scope.effectEstimateRows.push(row3);
              $scope.effectEstimateRows.push(row4);
            });
          });
        });
      }
    });

    function findValue(results, property) {
      return _.find(results, ['result_property', property]);
    }

    function buildResultsObject(armResults, endpoint, endpointUri, armUri) {
      if (endpoint.measurementType === 'ontology:dichotomous') {
        return {
          endpointUri: endpointUri,
          armUri: armUri,
          type: 'dichotomous',
          count: findValue(armResults, 'count').value,
          sampleSize: findValue(armResults, 'sample_size').value
        };
      } else if (endpoint.measurementType === 'ontology:continuous') {
        return {
          endpointUri: endpointUri,
          armUri: armUri,
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
