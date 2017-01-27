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

    function buildResultsObject(armResults, endpoint, endpointUri, armUri) {

      function findValue(results, property) {
        return _.find(results, ['result_property', property]);
      }

      var resultsObject = {
        endpointUri: endpointUri,
        armUri: armUri,
        type: endpoint.measurementType === 'ontology:dichotomous' ? 'dichotomous' : 'continuous'
      };

      _.forEach(endpoint.resultProperties, function(resultProperty) {
        var propertyName = resultProperty.split('#')[1];
        var propertyValue = findValue(armResults, propertyName);
        resultsObject[snakeToCamel(propertyName)] = propertyValue ? propertyValue.value : undefined;
      });
      return resultsObject;
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

    function snakeToCamel(snakeString) {
      return snakeString.replace(/_\w/g, function(m) {
        return m[1].toUpperCase();
      });
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

    return {
      buildResultsByEndpointAndArm: buildResultsByEndpointAndArm,
      buildEstimateRows: buildEstimateRows,
      buildResultsObject: buildResultsObject,
      buildResultLabel: buildResultLabel,
      buildArmTreatmentsLabel: buildArmTreatmentsLabel
    };
  };

  return dependencies.concat(D80TableService);

});
