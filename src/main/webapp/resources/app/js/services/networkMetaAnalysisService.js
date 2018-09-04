'use strict';
define(['lodash', 'angular'], function(_, angular) {
  var dependencies = ['$filter', 'AnalysisService'];

  var NetworkMetaAnalysisService = function($filter, AnalysisService) {

    var CONTINUOUS_TYPE = 'http://trials.drugis.org/ontology#continuous';
    var DICHOTOMOUS_TYPE = 'http://trials.drugis.org/ontology#dichotomous';
    var CATEGORICAL_TYPE = 'http://trials.drugis.org/ontology#categorical';
    var SURVIVAL_TYPE = 'http://trials.drugis.org/ontology#survival';

    function sortTableByStudyAndIntervention(table) {
      // sort table by studies and interventions
      var tableRowComparator = function(left, right) {
        if (left.study > right.study) {
          return 1;
        } else if (left.study < right.study) {
          return -1;
        }
        //studies equal then order by intervention, placing unmapped interventions last
        if (left.intervention === 'unmatched') {
          return 1;
        }
        if (right.intervention === 'unmatched') {
          return -1;
        }
        if (left.intervention > right.intervention) {
          return 1;
        } else if (left.intervention < right.intervention) {
          return -1;
        }
        return 0;
      };

      table.sort(tableRowComparator);
      return table;
    }

    // add information to render the table
    function addRenderingHintsToTable(table) {
      var currentStudy = 'null',
        currentInterventionRow = {
          intervention: null
        },
        dataRow;

      for (var i = 0; i < table.length; i++) {
        dataRow = table[i];
        if (dataRow.intervention !== currentInterventionRow.intervention || dataRow.intervention === 'unmatched') {
          dataRow.firstInterventionRow = true;
          currentInterventionRow = dataRow;
          currentInterventionRow.interventionRowSpan = 0;
        }

        if (dataRow.study !== currentStudy) {
          dataRow.firstStudyRow = true;
          dataRow.firstInterventionRow = true;
          currentStudy = dataRow.study;
          currentInterventionRow = dataRow;
          currentInterventionRow.interventionRowSpan = 0;
        }

        ++currentInterventionRow.interventionRowSpan;
        table[i] = dataRow;
      }

      return table;
    }

    function buildExcludedArmsMap(excludedArms) {
      return _.reduce(excludedArms, function(exclusions, excludedArm) {
        exclusions[excludedArm.trialverseUid] = true;
        return exclusions;
      }, {});
    }

    function getOutcomeMeasurement(analysis, trialDataArm, selectedMM) {
      if (selectedMM) {
        return _.find(trialDataArm.measurements[selectedMM.uri], function(measurement) {
          return analysis.outcome.semanticOutcomeUri === measurement.variableConceptUri;
        });
      }
      return null;
    }

    function buildTableFromTrialData(trialDataStudies, interventions, analysis, covariates, treatmentOverlapMap) {
      var dataRows = [];
      if (interventions.length < 1) {
        return dataRows;
      }
      var exclusionMap = buildExcludedArmsMap(analysis.excludedArms);
      angular.forEach(trialDataStudies, function(study) {

        var numberOfMatchedInterventions = 0;
        var numberOfIncludedInterventions = 0;
        var studyRows = [];

        _.forEach(study.trialDataArms, function(trialDataArm) {
          var dataRow = {};
          dataRow.measurementMoments = study.measurementMoments;
          dataRow.covariatesColumns = [];
          dataRow.study = study.name;
          dataRow.studyUri = study.studyUri;
          dataRow.studyUuid = dataRow.studyUri.slice(dataRow.studyUri.lastIndexOf('/') + 1);
          dataRow.studyRowSpan = study.trialDataArms.length;

          _.forEach(covariates, function(covariate) {
            if (covariate.isIncluded) {
              var covariateValue = _.find(study.covariateValues, function(covariateValue) {
                return covariateValue.covariateKey === covariate.definitionKey;
              }).value;
              var covariateColumn = {
                headerTitle: covariate.name,
                data: covariateValue === null ? 'NA' : covariateValue
              };
              dataRow.covariatesColumns.push(covariateColumn);
            }
          });
          dataRow.arm = trialDataArm.name;
          dataRow.trialverseUid = trialDataArm.uri;

          var overlappingTreatments;
          var intervention = _.find(interventions, function(intervention) {
            return _.find(trialDataArm.matchedProjectInterventionIds, function(id) {
              return id === intervention.id;
            });
          });

          if (intervention) {
            overlappingTreatments = treatmentOverlapMap[intervention.id];
            dataRow.intervention = intervention.name;
            dataRow.interventionId = intervention.id;
            ++numberOfMatchedInterventions;
          } else {
            dataRow.intervention = 'unmatched';
          }

          dataRow.included = !exclusionMap[trialDataArm.uri] && dataRow.intervention !== 'unmatched';
          if (dataRow.included) {
            ++numberOfIncludedInterventions;
          }

          if (dataRow.included && overlappingTreatments) {
            overlappingTreatments = [intervention].concat(overlappingTreatments);
            dataRow.overlappingInterventionWarning = _.map(overlappingTreatments, 'name').join(', ');
          }

          dataRow.measurements = measurementsByMM(analysis, trialDataArm, study.measurementMoments);

          studyRows.push(dataRow);
        });
        studyRows = studyRows.map(function(studyRow) {
          studyRow.numberOfMatchedInterventions = numberOfMatchedInterventions;
          studyRow.numberOfIncludedInterventions = numberOfIncludedInterventions;
          return studyRow;
        });

        dataRows = dataRows.concat(studyRows);
      });
      return dataRows;
    }

    function checkColumnsToShow(dataRows, measurementType) {
      var columnsToShow = {
        rate: (measurementType === 'dichotomous' || measurementType === 'survival'),
        mu: measurementType === 'continuous',
        sigma: shouldShowSigma(dataRows, measurementType),
        sampleSize: shouldShowN(dataRows, measurementType),
        stdErr: shouldShowStandardError(dataRows,measurementType),
        exposure: measurementType === 'survival'
      };
      return columnsToShow;
    }

    function shouldShowSigma(dataRows, measurementType) {
      if (measurementType !== 'continuous') {
        return false;
      }
      return !!_.find(dataRows, function(dataRow) {
        return _.find(dataRow.measurements, function(measurement) {
          return measurement.sigma !== 'NA';
        });
      });
    }

    function shouldShowN(dataRows, measurementType) {
      if (measurementType !== 'continuous' && measurementType !== 'dichotomous') {
        return false;
      }
      return !!_.find(dataRows, function(dataRow) {
        return _.find(dataRow.measurements, function(measurement) {
          return measurement.sampleSize !== 'NA';
        });
      });
    }

    function shouldShowStandardError(dataRows, measurementType){
      if (measurementType !== 'continuous') {
        return false;
      }
      return !!_.find(dataRows, function(dataRow) {
        return _.find(dataRow.measurements, function(measurement) {
          return measurement.stdErr !== 'NA' && measurement.stdErr !== null;
        });
      });
    }

    function getMeasurementType(trialData) {
      var studyWithMeasurement = _.find(trialData, function(study) {

        return _.find(study.trialDataArms, function(trialDataArm) {
          return _.find(trialDataArm.measurements, function(measurementsForMoment) {
            return _.find(measurementsForMoment, function(measurement) {
              return measurement.measurementTypeURI;
            });
          });
        });
      });
      return !studyWithMeasurement ? undefined : getRowMeasurementType(_.values(studyWithMeasurement.trialDataArms[0].measurements)[0][0]);
    }

    function measurementsByMM(analysis, trialDataArm, measurementMoments) {
      return _.reduce(measurementMoments, function(accum, measurementMoment) {
        var outcomeMeasurement = getOutcomeMeasurement(analysis, trialDataArm, measurementMoment);
        var sigma = toTableLabel(outcomeMeasurement, 'stdDev');
        if (sigma !== 'NA') {
          sigma = $filter('number')(sigma, 3);
        }
        accum[measurementMoment.uri] = {
          rate: toTableLabel(outcomeMeasurement, 'rate'),
          mu: toTableLabel(outcomeMeasurement, 'mean'),
          sigma: sigma,
          sampleSize: toTableLabel(outcomeMeasurement, 'sampleSize'),
          stdErr: toTableLabel(outcomeMeasurement, 'stdErr'),
          exposure: toTableLabel(outcomeMeasurement,'exposure'),
          type: getRowMeasurementType(outcomeMeasurement)
        };
        return accum;
      }, {});
    }

    function getRowMeasurementType(measurement) {
      if (!measurement) {
        return 'unknown';
      }
      var type = measurement.measurementTypeURI;
      return type.slice(type.lastIndexOf('#') + 1); // strip of http://blabla/ontology#
    }

    function toTableLabel(measurement, field) {
      if (!measurement) {
        return 'NA';
      }
      var value = measurement[field];
      var type = measurement.measurementTypeURI;
      var isMissing = isMissingValue(value, field, type);
      return isMissing ? 'NA' : value;
    }

    function isMissingValue(value, field, type) {
      if (value !== null && value !== undefined) {
        // has some value, therefore it can't be missing
        return false;
      }
      // FIXME: flexible fields && categoricals
      if (type === CONTINUOUS_TYPE) {
        if (field === 'mean' || field === 'stdDev' || field === 'sampleSize' || field === 'stdErr') {
          return true;
        }
      }
      if (type === DICHOTOMOUS_TYPE) {
        if (field === 'rate' || field === 'sampleSize') {
          return true;
        }
      }
      if (type === SURVIVAL_TYPE) {
        if(field ==='exposure') {
          return true;
        }
      }
      if (type === CATEGORICAL_TYPE) {
        return false;
      }
      return false;
    }

    function buildMissingValueByStudyMap(trialData, analysis, momentSelections) {
      // setup maps
      var excludedArmsByUri = buildExcludedArmsMap(analysis.excludedArms);

      // look for single missingValue
      return _.reduce(trialData, function(accum, trialDataStudy) {
        var nMatchedInterventions = countMatchedInterventions(trialDataStudy, excludedArmsByUri);
        if (nMatchedInterventions < 2) { // if there's not enough matched interventions we don't care
          accum[trialDataStudy.studyUri] = false;
          return accum;
        }
        var selectedMM = momentSelections[trialDataStudy.studyUri];
        accum[trialDataStudy.studyUri] = _.find(trialDataStudy.trialDataArms, function(trialDataArm) {
          if (excludedArmsByUri[trialDataArm.uri]) {
            return false; //excluded arm, therefore missing values don't count
          }
          if (trialDataArm.matchedProjectInterventionIds.length < 1) {
            return false; //no matched interventions, therefore missing values don't count
          }
          var measurement = getOutcomeMeasurement(analysis, trialDataArm, selectedMM);
          if (measurement) {
            var measurementType = measurement.measurementTypeURI;

            var isMeanMissing = isMissingValue(measurement.mean, 'mean', measurementType);
            var isStdDevMissing = isMissingValue(measurement.stdDev, 'stdDev', measurementType);
            var isSampleSizeMissing = isMissingValue(measurement.sampleSize, 'sampleSize', measurementType);
            var isStdErrMissing = isMissingValue(measurement.stdErr, 'stdErr', measurementType);
            var isRateMissing = isMissingValue(measurement.rate, 'rate', measurementType);

            return isMeanMissing || isRateMissing ||
              (isStdDevMissing && isStdErrMissing) ||
              (isSampleSizeMissing && isStdErrMissing);
          } else {
            return true;
          }
        });
        return accum;
      }, {});
    }

    function countMatchedInterventions(study, excludedArmsByUri) {
      var numberOfMatchedInterventions = 0;
      angular.forEach(study.trialDataArms, function(trialDataArm) {
        if (!excludedArmsByUri[trialDataArm.uri]) {
          numberOfMatchedInterventions += trialDataArm.matchedProjectInterventionIds.length;
        }
      });
      return numberOfMatchedInterventions;
    }

    function getValidStudies(trialData, analysis) {
      var excludedArmsByUri = buildExcludedArmsMap(analysis.excludedArms);

      return _.filter(trialData, function(study) {
        return countMatchedInterventions(study, excludedArmsByUri) > 1;
      });
    }

    function filterExcludedArms(trialDataStudies, excludedArms) {
      var exclusionMap = buildExcludedArmsMap(excludedArms);
      return _.map(trialDataStudies, function(study) {
        var copiedStudy = angular.copy(study);
        copiedStudy.trialDataArms = _.filter(study.trialDataArms, function(arm) {
          return !exclusionMap[arm.uri];
        });
        return copiedStudy;
      });
    }

    function sumInterventionSampleSizes(trialData, intervention, analysis, momentSelections) {
      var interventionSum = _.reduce(trialData, function(sum, trialDataStudy) {
        var selectedMM = momentSelections[trialDataStudy.studyUri];
        angular.forEach(trialDataStudy.trialDataArms, function(trialDataArm) {

          var matchedIntervention = _.find(trialDataArm.matchedProjectInterventionIds, function(id) {
            return id === intervention.id;
          });

          if (matchedIntervention) {
            var outcomeMeasurement = getOutcomeMeasurement(analysis, trialDataArm, selectedMM);
            sum += outcomeMeasurement ? outcomeMeasurement.sampleSize : 0;
          }
        });
        return sum;
      }, 0);
      return interventionSum;
    }

    function findArmForIntervention(trialdataArms, trialDataIntervention) {
      return _.find(trialdataArms, function(trialdataArm) {
        return _.find(trialdataArm.matchedProjectInterventionIds, function(id) {
          return id === trialDataIntervention.id;
        });
      });
    }

    function studyMeasuresBothInterventions(trialDataStudy, fromIntervention, toIntervention) {
      return fromIntervention && toIntervention &&
        findArmForIntervention(trialDataStudy.trialDataArms, fromIntervention) &&
        findArmForIntervention(trialDataStudy.trialDataArms, toIntervention);
    }

    function attachStudiesForEdges(edges, trialData) {
      return _.map(edges, function(edge) {
        edge.studies = _.filter(trialData, function(trialDataStudy) {
          return studyMeasuresBothInterventions(trialDataStudy, edge.from, edge.to);
        });
        return edge;
      });
    }


    function transformTrialDataToNetwork(trialDataStudies, interventions, analysis, momentSelections) {
      var network = {
        interventions: [],
        edges: AnalysisService.generateEdges(interventions)
      };
      var validTrialData = filterExcludedArms(trialDataStudies, analysis.excludedArms);
      validTrialData = getValidStudies(validTrialData, analysis);

      network.interventions = _.map(interventions, function(intervention) {
        return {
          name: intervention.name,
          sampleSize: sumInterventionSampleSizes(validTrialData, intervention, analysis, momentSelections)
        };
      });
      network.edges = attachStudiesForEdges(network.edges, validTrialData);
      network.edges = _.filter(network.edges, function(edge) {
        return edge.studies.length > 0;
      });
      return network;
    }

    function transformTrialDataToTableRows(trialData, interventions, analysis, covariates, treatmentOverlapMap) {
      var tableRows = buildTableFromTrialData(trialData, interventions, analysis, covariates, treatmentOverlapMap);
      tableRows = sortTableByStudyAndIntervention(tableRows);
      tableRows = addRenderingHintsToTable(tableRows);
      return tableRows;
    }

    function buildInterventionInclusions(interventions, analysis) {
      return _.reduce(interventions, function(accumulator, intervention) {
        if (intervention.isIncluded) {
          accumulator.push({
            analysisId: analysis.id,
            interventionId: intervention.id
          });
        }
        return accumulator;
      }, []);
    }

    function doesModelHaveAmbiguousArms(trialDataStudies, analysis) {
      return _.find(trialDataStudies, function(study) {
        return _.find(analysis.interventionInclusions, function(inclusion) {
          return doesInterventionHaveAmbiguousArms(inclusion.interventionId, study.studyUri, trialDataStudies, analysis);
        });
      });
    }

    function doesInterventionHaveAmbiguousArms(interventionId, studyUri, trialDataStudies, analysis) {
      if (interventionId === null) {
        return false;
      }

      function isArmIncluded(trialDataArm) {
        return !_.find(analysis.excludedArms, function(exclusion) {
          return exclusion.trialverseUid === trialDataArm.uri;
        });
      }
      var containingStudy = _.find(trialDataStudies, function(trialDataStudy) {
        return trialDataStudy.studyUri === studyUri;
      });

      var includedArmsForDrugUid = _.filter(containingStudy.trialDataArms, function(trialDataArm) {
        var matchedIntervention = _.find(trialDataArm.matchedProjectInterventionIds, function(id) {
          return id === interventionId;
        });
        return matchedIntervention !== undefined && isArmIncluded(trialDataArm);
      });

      return includedArmsForDrugUid.length > 1;
    }

    function addInclusionsToInterventions(interventions, inclusions) {
      var inclusionMap = _.fromPairs(_.map(inclusions, function(inclusion) {
        return [inclusion.interventionId, true];
      }));

      return interventions.map(function(intervention) {
        var withInclusion = angular.copy(intervention);
        withInclusion.isIncluded = inclusionMap[intervention.id];
        return withInclusion;
      });
    }

    function addInclusionsToCovariates(covariates, inclusions) {
      var inclusionMap = _.fromPairs(_.map(inclusions, function(inclusion) {
        return [inclusion.covariateId, true];
      }));

      return covariates.map(function(covariate) {
        var withInclusion = angular.copy(covariate);
        withInclusion.isIncluded = inclusionMap[covariate.id];
        return withInclusion;
      });
    }

    function cleanUpExcludedArms(excludedIntervention, analysis, trialDataStudies) {
      return _.reject(analysis.excludedArms, function(armExclusion) {
        return _.find(trialDataStudies, function(study) {
          var armsMatchingExcludedIntervention = _.find(study.trialDataArms, function(arm) {
            return arm.uri === armExclusion.trialverseUid &&
              (arm.matchedProjectInterventionIds.lastIndexOf(excludedIntervention.id) > -1);
          });
          return armsMatchingExcludedIntervention;
        });
      });
    }

    function addOverlaps(overlappingTreatmentsMap, interventionIds) {
      interventionIds.forEach(function(interventionId) {
        var idsWithoutSelf = _.without(interventionIds, interventionId);
        if (!overlappingTreatmentsMap[interventionId]) {
          overlappingTreatmentsMap[interventionId] = [];
        }
        overlappingTreatmentsMap[interventionId] = _.uniq(overlappingTreatmentsMap[interventionId].concat(idsWithoutSelf));
      });
      return overlappingTreatmentsMap;
    }

    function buildOverlappingTreatmentMap(interventions, trialDataStudies) {
      var includedInterventions = _.filter(interventions, 'isIncluded');
      var includedInterventionMap = _.keyBy(includedInterventions, 'id');
      var overlappingTreatmentsMap = {};

      trialDataStudies.forEach(function(study) {
        study.trialDataArms.forEach(function(arm) {
          if (arm.matchedProjectInterventionIds.length > 1) {
            overlappingTreatmentsMap = addOverlaps(overlappingTreatmentsMap, arm.matchedProjectInterventionIds);
          }
        });
      });

      overlappingTreatmentsMap = _.reduce(overlappingTreatmentsMap, function(accum, value, key) {
        accum[key] = value.map(function(interventionId) {
          return includedInterventionMap[interventionId];
        });
        return accum;
      }, overlappingTreatmentsMap);

      return overlappingTreatmentsMap;
    }

    function getIncludedInterventions(interventions) {
      return _.filter(interventions, function(intervention) {
        return intervention.isIncluded;
      });
    }

    function doesModelHaveInsufficientCovariateValues(trialData) {
      var covariateValues = _.reduce(trialData, function(accum, trialDatum) {
        _.forEach(trialDatum.covariatesColumns, function(covariateColumn) {
          if (!accum[covariateColumn.headerTitle]) {
            accum[covariateColumn.headerTitle] = [];
          }
          accum[covariateColumn.headerTitle] = _.uniq(accum[covariateColumn.headerTitle].concat(covariateColumn.data));
        });

        return accum;
      }, {});
      return _.find(covariateValues, function(values) {
        return _.uniq(values).length <= 1;
      });
    }

    function changeCovariateInclusion(covariate, analysis) {
      var includedCovariates = analysis.includedCovariates;
      var updatedList = angular.copy(includedCovariates);
      if (covariate.isIncluded) {
        updatedList.push({
          analysisId: analysis.id,
          covariateId: covariate.id
        });
      } else {
        _.remove(updatedList, function(includedCovariate) {
          return includedCovariate.covariateId === covariate.id;
        });
      }
      return updatedList;
    }

    function changeArmExclusion(dataRow, analysis) {
      if (dataRow.included) {
        for (var i = 0; i < analysis.excludedArms.length; ++i) {
          if (analysis.excludedArms[i].trialverseUid === dataRow.trialverseUid) {
            analysis.excludedArms.splice(i, 1);
            break;
          }
        }
      } else {
        analysis.excludedArms.push({
          analysisId: analysis.id,
          trialverseUid: dataRow.trialverseUid
        });
      }
      return analysis;
    }

    function buildMomentSelections(trialData, analysis) {
      return _.reduce(trialData, function(accum, study) {
        var selected = _.find(analysis.includedMeasurementMoments, function(selectedMM) {
          return selectedMM.study === study.studyUri;
        });
        var selectedMMUri = selected ? selected.measurementMoment : study.defaultMeasurementMoment;

        var selectedMM = _.find(study.measurementMoments, function(measurementMoment) {
          return measurementMoment.uri === selectedMMUri;
        });
        if (selectedMM) { // no data for selected measurement moment means it's not on the study
          selectedMM.isDefault = selected ? false : true;
        }
        accum[study.studyUri] = selectedMM;
        return accum;
      }, {});
    }

    return {
      addInclusionsToCovariates: addInclusionsToCovariates,
      addInclusionsToInterventions: addInclusionsToInterventions,
      buildInterventionInclusions: buildInterventionInclusions,
      buildMissingValueByStudyMap: buildMissingValueByStudyMap,
      buildMomentSelections: buildMomentSelections,
      buildOverlappingTreatmentMap: buildOverlappingTreatmentMap,
      changeArmExclusion: changeArmExclusion,
      changeCovariateInclusion: changeCovariateInclusion,
      checkColumnsToShow: checkColumnsToShow,
      getMeasurementType: getMeasurementType,
      cleanUpExcludedArms: cleanUpExcludedArms,
      doesInterventionHaveAmbiguousArms: doesInterventionHaveAmbiguousArms,
      doesModelHaveAmbiguousArms: doesModelHaveAmbiguousArms,
      doesModelHaveInsufficientCovariateValues: doesModelHaveInsufficientCovariateValues,
      getIncludedInterventions: getIncludedInterventions,
      transformTrialDataToNetwork: transformTrialDataToNetwork,
      transformTrialDataToTableRows: transformTrialDataToTableRows
    };
  };
  return dependencies.concat(NetworkMetaAnalysisService);
});
