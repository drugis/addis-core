define(['lodash'], function (_) {
  var dependencies = ['$filter'];
  var D80TableService = function ($filter) {
    var exponentialFilter = $filter('exponentialFilter'),
      durationFilter = $filter('durationFilter');

    function buildMeasurements(results, measurementMomentUri, endpoints) {
      var endpointsByUri = _.keyBy(endpoints, 'uri');
      var resultsByEndpointAndArm = buildResultsByEndpointAndArm(
        results,
        measurementMomentUri
      );
      var measurements = _.mapValues(
        resultsByEndpointAndArm,
        (endPointResultsByArm, endpointUri) => {
          return _.mapValues(endPointResultsByArm, (armResults, armUri) => {
            var resultsObject = buildResultsObject(
              armResults,
              endpointsByUri[endpointUri],
              armUri
            );
            return resultsObject;
          });
        }
      );
      measurements.toBackEndMeasurements = _(measurements)
        .values()
        .flatMap((row) => {
          return _.map(row, (measurement) => {
            return _.omit(measurement, 'label');
          });
        })
        .value();
      return measurements;
    }

    function buildResultsByEndpointAndArm(results, measurementMomentUri) {
      return _.reduce(
        results,
        function (accum, resultsForOutcome) {
          accum = _.chain(resultsForOutcome)
            .filter(['momentUri', measurementMomentUri])
            .reduce(function (innerAccum, result) {
              if (!innerAccum[result.outcomeUri]) {
                innerAccum[result.outcomeUri] = {};
              }
              if (!innerAccum[result.outcomeUri][result.armUri]) {
                innerAccum[result.outcomeUri][result.armUri] = [];
              }
              innerAccum[result.outcomeUri][result.armUri].push(result);
              return innerAccum;
            }, accum)
            .value();
          return accum;
        },
        {}
      );
    }

    function buildEstimateRows(estimateResults, endpoints, arms) {
      var resultRows = [];
      var subjectArms = removebaseline(arms, estimateResults.baselineUri);

      _.forEach(endpoints, function (endpoint) {
        var comparisonGroupsRow = {
          endpoint: endpoint,
          rowLabel: 'Comparison Groups',
          rowValues: []
        };
        var pointEstimateRow = {
          rowLabel:
            endpoint.measurementType === 'ontology:dichotomous'
              ? 'Risk ratio'
              : 'Mean difference',
          rowValues: []
        };
        var confidenceIntervalRow = {
          rowLabel: 'Confidence Interval',
          rowValues: []
        };
        var pValueRow = {
          rowLabel: 'P-value',
          rowValues: []
        };

        _.forEach(subjectArms, function (arm) {
          var estimate;
          if (estimateResults.estimates) {
            estimate = _.find(estimateResults.estimates[endpoint.uri], [
              'armUri',
              arm.armURI
            ]);
          }
          comparisonGroupsRow.rowValues.push(arm.label);
          pointEstimateRow.rowValues.push(
            estimate ? estimate.pointEstimate.toFixed(2) : '<point estimate>'
          );
          confidenceIntervalRow.rowValues.push(
            estimate
              ? '(' +
                  estimate.confidenceIntervalLowerBound.toFixed(2) +
                  ', ' +
                  estimate.confidenceIntervalUpperBound.toFixed(2) +
                  ')'
              : '<confidence interval>'
          );
          pValueRow.rowValues.push(
            estimate ? estimate.pValue.toFixed(2) : '<P-value>'
          );
        });

        resultRows.push(comparisonGroupsRow);
        resultRows.push(pointEstimateRow);
        resultRows.push(confidenceIntervalRow);
        resultRows.push(pValueRow);
      });
      return resultRows;
    }

    function removebaseline(arms, baselineUri) {
      return _.reject(arms, ['armURI', baselineUri]);
    }

    function buildResultsObject(armResults, endpoint, armUri) {
      var result = {
        endpointUri: endpoint.uri,
        armUri: armUri,
        type:
          endpoint.measurementType === 'ontology:dichotomous'
            ? 'dichotomous'
            : 'continuous',
        resultProperties: {}
      };
      result.resultProperties = getValuesForProperties(
        endpoint.resultProperties,
        armResults
      );
      result.label = buildResultLabel(result);
      return result;
    }

    function getValuesForProperties(resultProperties, armResults) {
      return _.reduce(
        resultProperties,
        function (accum, resultProperty) {
          var propertyName = resultProperty.split('#')[1];
          var propertyValue = findValue(armResults, propertyName);
          accum[snakeToCamel(propertyName)] = propertyValue
            ? propertyValue.value
            : undefined;
          return accum;
        },
        {}
      );
    }

    function findValue(results, property) {
      return _.find(results, ['result_property', property]);
    }

    function buildResultLabel(result) {
      if (result.type === 'dichotomous') {
        return createDichotomousResultLabel(result);
      } else if (result.type === 'continuous') {
        return createContinuousResultLabel(result);
      } else {
        throw 'unknown measurement type';
      }
    }

    function createDichotomousResultLabel(result) {
      var sampleSize = result.resultProperties.sampleSize;
      var count = result.resultProperties.count;
      var percentage = result.resultProperties.percentage;
      if (!count && percentage) {
        count = estimateCount(percentage, sampleSize);
      }
      return count && sampleSize
        ? count + '/' + sampleSize
        : '<count> <sample size>';
    }

    function estimateCount(percentage, sampleSize) {
      return ((percentage * sampleSize) / 100).toFixed(0);
    }

    function createContinuousResultLabel(result) {
      var sampleSize = result.resultProperties.sampleSize;
      var mean = result.resultProperties.mean;
      var standardDeviation = result.resultProperties.standardDeviation;
      var standardError = result.resultProperties.standardError;
      if (!standardDeviation && standardError) {
        standardDeviation = calculateStandardDeviation(
          standardError,
          sampleSize
        );
      }
      return mean && standardDeviation && sampleSize
        ? exponentialFilter(mean).toFixed(2) +
            ' ± ' +
            exponentialFilter(standardDeviation).toFixed(2) +
            ' (' +
            sampleSize +
            ')'
        : '<point estimate> <variability> <n>';
    }

    function calculateStandardDeviation(standardError, sampleSize) {
      return standardError * Math.sqrt(sampleSize);
    }

    function buildArmTreatmentsLabel(treatments) {
      var treatmentLabels = _.map(treatments, function (treatment) {
        if (treatment.treatmentDoseType === 'ontology:FixedDoseDrugTreatment') {
          return createFixedDoseTreatementLabel(treatment);
        } else if (
          treatment.treatmentDoseType === 'ontology:TitratedDoseDrugTreatment'
        ) {
          return createTitratedDoseTreatmentLabel(treatment);
        } else {
          throw 'unknown dosage type';
        }
      });
      return treatmentLabels.join(' + ');
    }

    function createFixedDoseTreatementLabel(treatment) {
      return (
        treatment.drug.label +
        ' ' +
        exponentialFilter(treatment.fixedValue) +
        ' ' +
        treatment.doseUnit.label +
        ' per ' +
        durationFilter(treatment.dosingPeriodicity)
      );
    }

    function createTitratedDoseTreatmentLabel(treatment) {
      return (
        treatment.drug.label +
        ' ' +
        exponentialFilter(treatment.minValue) +
        '-' +
        exponentialFilter(treatment.maxValue) +
        ' ' +
        treatment.doseUnit.label +
        ' per ' +
        durationFilter(treatment.dosingPeriodicity)
      );
    }

    function snakeToCamel(snakeString) {
      return snakeString.replace(/_\w/g, function (m) {
        return m[1].toUpperCase();
      });
    }

    function getInitialMeasurementMoment(measurementMoments, primaryEpoch) {
      var primaryMeasurementMoment;
      if (primaryEpoch) {
        primaryMeasurementMoment = _.find(measurementMoments, function (
          measurementMoment
        ) {
          return (
            measurementMoment.offset === 'PT0S' &&
            measurementMoment.relativeToAnchor === 'ontology:anchorEpochEnd' &&
            measurementMoment.epochUri === primaryEpoch.uri
          );
        });
      }
      return primaryMeasurementMoment && measurementMoments
        ? primaryMeasurementMoment
        : measurementMoments[0];
    }

    function getActivityForArm(
      arm,
      activities,
      designCoordinates,
      primaryEpoch
    ) {
      if (designCoordinates.length && activities.length) {
        var activityUri = getActivityUri(arm, designCoordinates, primaryEpoch);
        return _.find(activities, function (activity) {
          return activity.activityUri === activityUri;
        });
      } else {
        return undefined;
      }
    }

    function getActivityUri(arm, designCoordinates, primaryEpoch) {
      return _.find(designCoordinates, function (coordinate) {
        return (
          coordinate.armUri === arm.armURI &&
          coordinate.epochUri === primaryEpoch.uri
        );
      }).activityUri;
    }

    return {
      buildResultsByEndpointAndArm: buildResultsByEndpointAndArm,
      buildMeasurements: buildMeasurements,
      buildEstimateRows: buildEstimateRows,
      buildResultsObject: buildResultsObject,
      buildResultLabel: buildResultLabel,
      buildArmTreatmentsLabel: buildArmTreatmentsLabel,
      getInitialMeasurementMoment: getInitialMeasurementMoment,
      getActivityForArm: getActivityForArm
    };
  };

  return dependencies.concat(D80TableService);
});
