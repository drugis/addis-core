define(['lodash'], function(_) {

  var dependencies = ['$filter'];
  var D80TableService = function($filter) {
    var exponentialFilter = $filter('exponentialFilter'),
      durationFilter = $filter('durationFilter');

    function buildResultsByEndpointAndArm(results, primaryMeasurementMomentUri) {
      var resultsByEndpointAndArm = {};
      _.forEach(results, function(resultsForOutcome) {
        resultsByEndpointAndArm = _.chain(resultsForOutcome)
          .filter(['momentUri', primaryMeasurementMomentUri])
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
      return resultsByEndpointAndArm;
    }

    function buildMeasurements(results, primaryMeasurementMomentUri, endpoints) {
      var endpointsByUri = _.keyBy(endpoints, 'uri');

      var resultsByEndpointAndArm = buildResultsByEndpointAndArm(results, primaryMeasurementMomentUri);

      var toBackEndMeasurements = [];
      var measurements = _.reduce(resultsByEndpointAndArm, function(accum, endPointResultsByArm, endpointUri) {
        accum[endpointUri] = _.reduce(endPointResultsByArm, function(accum, armResults, armUri) {
          var resultsObject = buildResultsObject(armResults, endpointsByUri[endpointUri], armUri);
          resultsObject.label = buildResultLabel(resultsObject);
          toBackEndMeasurements.push(resultsObject);
          accum[armUri] = resultsObject;
          return accum;
        }, {});
        return accum;
      }, {});
      measurements.toBackEndMeasurements = toBackEndMeasurements;
      return measurements;
    }

    function buildEstimateRows(estimateResults, endpoints, arms) {
      var resultRows = [];
      var subjectArms = _.reject(arms, ['armURI', estimateResults.baselineUri]);

      _.forEach(endpoints, function(endpoint) {
        var comparisonGroupsRow = {
          endpoint: endpoint,
          rowLabel: 'Comparison Groups',
          rowValues: []
        };
        var pointEstimateRow = {
          rowLabel: endpoint.measurementType === 'ontology:dichotomous' ? 'Risk ratio' : 'Mean difference',
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

        _.forEach(subjectArms, function(arm) {
          var estimate = _.find(estimateResults.estimates[endpoint.uri], ['armUri', arm.armURI]);
          comparisonGroupsRow.rowValues.push(
            arm.label
          );
          pointEstimateRow.rowValues.push(
            estimate ? estimate.pointEstimate.toFixed(2) : '<point estimate>'
          );
          confidenceIntervalRow.rowValues.push(
            estimate ? '(' + estimate.confidenceIntervalLowerBound.toFixed(2) + ', ' + estimate.confidenceIntervalUpperBound.toFixed(2) + ')' : '<confidence interval>'
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

    function buildResultsObject(armResults, endpoint, armUri) {

      function findValue(results, property) {
        return _.find(results, ['result_property', property]);
      }

      var resultsObject = {
        endpointUri: endpoint.uri,
        armUri: armUri,
        type: endpoint.measurementType === 'ontology:dichotomous' ? 'dichotomous' : 'continuous',
        resultProperties: {}
      };

      _.forEach(endpoint.resultProperties, function(resultProperty) {
        var propertyName = resultProperty.split('#')[1];
        var propertyValue = findValue(armResults, propertyName);
        resultsObject.resultProperties[snakeToCamel(propertyName)] = propertyValue ? propertyValue.value : undefined;
      });
      return resultsObject;
    }

    function buildResultLabel(resultsObject) {
      var sampleSize;
      if (resultsObject.type === 'dichotomous') {
        sampleSize = resultsObject.resultProperties.sampleSize;
        var count = resultsObject.resultProperties.count;
        var percentage = resultsObject.resultProperties.percentage;
        if(!count && percentage){
          count = ((percentage * sampleSize) / 100).toFixed(0);
        }
        return count && sampleSize ?
          count + '/' + sampleSize :
          '<count> <sample size>';
      } else if (resultsObject.type === 'continuous') {
        sampleSize = resultsObject.resultProperties.sampleSize;
        var mean = resultsObject.resultProperties.mean;
        var standardDeviation = resultsObject.resultProperties.standardDeviation;
        var standardError = resultsObject.resultProperties.standardError;
        if (!standardDeviation && standardError) {
          standardDeviation = standardError * Math.sqrt(sampleSize);
        }
        return mean && standardDeviation && sampleSize ?
          exponentialFilter(mean).toFixed(2) + ' ± ' + exponentialFilter(standardDeviation).toFixed(2) + ' (' + sampleSize + ')' :
          '<point estimate> <variability> <n>';
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
            '-' + exponentialFilter(treatment.maxValue) + ' ' + treatment.doseUnit.label + ' per ' +
            durationFilter(treatment.dosingPeriodicity);
        } else {
          throw ('unknown dosage type');
        }
      });
      return treatmentLabels.join(' + ');
    }

    function snakeToCamel(snakeString) {
      return snakeString.replace(/_\w/g, function(m) {
        return m[1].toUpperCase();
      });
    }

    return {
      buildResultsByEndpointAndArm: buildResultsByEndpointAndArm,
      buildMeasurements: buildMeasurements,
      buildEstimateRows: buildEstimateRows,
      buildResultsObject: buildResultsObject,
      buildResultLabel: buildResultLabel,
      buildArmTreatmentsLabel: buildArmTreatmentsLabel
    };
  };

  return dependencies.concat(D80TableService);

});