'use strict';
define(['lodash', 'angular'], function(_, angular) {
  var dependencies = [
    '$filter',
    'AnalysisService'
  ];

  var NetworkMetaAnalysisService = function(
    $filter,
    AnalysisService
  ) {
    var CONTRAST_RESULT_PROPERTIES = [
      'meanDifference',
      'stdErr'
    ];
    var ABSOLUTE_RESULT_PROPERTIES = [
      'stdErr',
      'stdDev',
      'sampleSize',
      'rate',
      'mean',
      'exposure'
    ];

    function transformStudiesToTableRows(studies, interventions, analysis, covariates, treatmentOverlap) {
      var rows = buildTableRowsFromStudies(studies, interventions, analysis, covariates, treatmentOverlap);
      var partitionedRows = partitionTableRows(rows);

      var absoluteRows = sortTableByStudyAndIntervention(partitionedRows[1]);
      var contrastRows = sortTableByStudyAndIntervention(partitionedRows[0]);
      absoluteRows = addRenderingHintsToTable(absoluteRows);
      contrastRows = addRenderingHintsToTable(contrastRows);
      return { contrast: contrastRows, absolute: absoluteRows };
    }

    function partitionTableRows(rows) {
      return _.partition(rows, function(row) {
        return row.isContrastArm;
      });
    }

    function buildTableRowsFromStudies(studies, interventions, analysis, covariates, treatmentOverlap) {
      if (interventions.length < 1) {
        return [];
      }
      return buildRows(studies, covariates, interventions, treatmentOverlap, analysis);
    }

    function buildRows(studies, covariates, interventions, treatmentOverlap, analysis) {
      var exclusionMap = buildExcludedArmsMap(analysis.excludedArms);
      return _.reduce(studies, function(accum, study) {
        var studyRows = _.map(study.arms, _.partial(
          createDataRow, study, covariates, interventions, treatmentOverlap, exclusionMap, analysis));

        var numberOfMatchedInterventions = countMatchedInterventionsForRows(studyRows);
        var numberOfIncludedInterventions = countIncludedInterventions(studyRows);
        studyRows = studyRows.map(function(studyRow) {
          studyRow.numberOfMatchedInterventions = numberOfMatchedInterventions;
          studyRow.numberOfIncludedInterventions = numberOfIncludedInterventions;
          return studyRow;
        });

        return accum.concat(studyRows);
      }, []);
    }

    function createDataRow(study, covariates, interventions, treatmentOverlapMap, exclusionMap, analysis, arm) {
      var row = {};
      row.measurementMoments = study.measurementMoments;
      row.covariatesColumns = [];
      row.study = study.name;
      row.studyUri = study.studyUri;
      row.studyUuid = row.studyUri.slice(row.studyUri.lastIndexOf('/') + 1);
      row.studyRowSpan = study.arms.length;
      row.covariatesColumns = _(covariates)
        .map(_.partial(createCovariateColumn, study))
        .compact()
        .value();
      row.arm = arm.name;
      row.trialverseUid = arm.uri;
      var overlappingTreatments;
      var intervention = _.find(interventions, function(intervention) {
        return _.find(arm.matchedProjectInterventionIds, function(id) {
          return id === intervention.id;
        });
      });
      if (intervention) {
        overlappingTreatments = treatmentOverlapMap[intervention.id];
        row.intervention = intervention.name;
        row.interventionId = intervention.id;
      } else {
        row.intervention = 'unmatched';
      }
      row.included = !exclusionMap[arm.uri] && row.intervention !== 'unmatched';
      if (row.included && overlappingTreatments) {
        overlappingTreatments = [intervention].concat(overlappingTreatments);
        row.overlappingInterventionWarning = _.map(overlappingTreatments, 'name').join(', ');
      }
      _.merge(row, getReferenceParameters(analysis, arm, study.measurementMoments));
      var isReference = isReferenceRow(row, study);
      row.isContrastArm = !!row.referenceArm || isReference;
      if (!isReference) {
        row.measurements = measurementsByMM(analysis, arm, study.measurementMoments);
      }
      return row;
    }

    function isReferenceRow(row, study) {
      return _.some(study.arms, function(arm) {
        return _.some(study.measurementMoments, function(moment) {
          return _.some(arm.measurements[moment.uri], function(measurement) {
            return measurement.referenceArm && measurement.referenceArm === row.trialverseUid;
          });
        });
      });
    }

    function getReferenceParameters(analysis, arm, measurementMoments) {
      var returnValue = {};
      _.forEach(measurementMoments, function(measurementMoment) {
        var measurementForOutcome = getMeasurementForOutcome(analysis, arm, measurementMoment);
        if (measurementForOutcome && measurementForOutcome.referenceArm) {
          returnValue = {
            referenceArm: measurementForOutcome.referenceArm,
            referenceStdErr: measurementForOutcome.referenceStdErr
          };
        }
      });
      return returnValue;
    }

    function measurementsByMM(analysis, arm, measurementMoments) {
      return _.reduce(measurementMoments, function(accum, measurementMoment) {
        var measurementForOutcome = getMeasurementForOutcome(analysis, arm, measurementMoment);
        if (measurementForOutcome && measurementForOutcome.referenceArm) {
          accum[measurementMoment.uri] = getContrastMeasurement(measurementForOutcome);
        } else {
          accum[measurementMoment.uri] = getAbsoluteMeasurement(measurementForOutcome);
        }
        return accum;
      }, {});
    }

    function getAbsoluteMeasurement(measurement) {
      var sigma = toTableLabel(measurement, 'stdDev');
      if (sigma !== 'NA') {
        sigma = $filter('number')(sigma, 3);
      }
      return {
        rate: toTableLabel(measurement, 'rate'),
        mu: toTableLabel(measurement, 'mean'),
        sigma: sigma,
        sampleSize: toTableLabel(measurement, 'sampleSize'),
        stdErr: toTableLabel(measurement, 'stdErr'),
        exposure: toTableLabel(measurement, 'exposure'),
        type: getRowMeasurementType(measurement)
      };
    }

    function getContrastMeasurement(measurement) {
      return {
        stdErr: $filter('number')(toTableLabel(measurement, 'stdErr'), 3),
        referenceArm: toTableLabel(measurement, 'referenceArm'),
        referenceStdErr: toTableLabel(measurement, 'referenceStdErr'),
        meanDifference: toMeanDifferenceLabel(measurement),
        type: getRowMeasurementType(measurement)
      };
    }

    function getMeasurementForOutcome(analysis, arm, measurementMoment) {
      if (measurementMoment) {
        return _.find(arm.measurements[measurementMoment.uri], function(measurement) {
          return analysis.outcome.semanticOutcomeUri === measurement.variableConceptUri;
        });
      }
      return null;
    }

    function toMeanDifferenceLabel(measurement) {
      if (!measurement) {
        return 'NA';
      }
      if (hasValue(measurement.meanDifference)) {
        return measurement.meanDifference;
      }
      if (hasValue(measurement.standardizedMeanDifference)) {
        return measurement.standardizedMeanDifference;
      }
      if (hasValue(measurement.oddsRatio)) {
        return $filter('number')(measurement.oddsRatio, 3);
      }
      if (hasValue(measurement.riskRatio)) {
        return $filter('number')(measurement.riskRatio, 3);
      }
      if (hasValue(measurement.hazardRatio)) {
        return $filter('number')(measurement.hazardRatio, 3);
      }
      return 'NA';
    }

    function toTableLabel(measurement, field) {
      if (!measurement) { return 'NA'; }
      var value = measurement[field];
      var isMissing = !hasValue(measurement[field]);
      return isMissing ? 'NA' : value;
    }


    function hasValue(value) {
      return value !== null && value !== undefined && value !== 'NA';
    }

    function sortTableByStudyAndIntervention(table) {
      table.sort(sortByStudiesAndInterventions);
      return table;
    }

    function sortByStudiesAndInterventions(left, right) {
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
    }

    function addRenderingHintsToTable(rows) {
      var currentStudy = 'null';
      var currentInterventionRow = {
        intervention: null
      };
      var row;

      for (var i = 0; i < rows.length; i++) {
        row = rows[i];
        if (row.intervention !== currentInterventionRow.intervention || row.intervention === 'unmatched') {
          row.firstInterventionRow = true;
          currentInterventionRow = row;
          currentInterventionRow.interventionRowSpan = 0;
        }

        if (row.study !== currentStudy) {
          row.firstStudyRow = true;
          row.firstInterventionRow = true;
          currentStudy = row.study;
          currentInterventionRow = row;
          currentInterventionRow.interventionRowSpan = 0;
        }
        ++currentInterventionRow.interventionRowSpan;
        rows[i] = row;
      }

      return rows;
    }

    function buildExcludedArmsMap(excludedArms) {
      return _.reduce(excludedArms, function(exclusions, excludedArm) {
        exclusions[excludedArm.trialverseUid] = true;
        return exclusions;
      }, {});
    }

    function countIncludedInterventions(studyRows) {
      return _(studyRows).filter(function(row) {
        return row.included;
      }).size();
    }

    function countMatchedInterventionsForRows(studyRows) {
      return studyRows.length - _(studyRows).filter(function(row) {
        return row.intervention === 'unmatched';
      }).size();
    }

    function createCovariateColumn(study, covariate) {
      if (covariate.isIncluded) {
        var studyCovariate = _.find(study.covariateValues, function(covariateValue) {
          return covariateValue.covariateKey === covariate.definitionKey;
        });
        var covariateValue = studyCovariate ? studyCovariate.value : null;
        var covariateColumn = {
          headerTitle: covariate.name,
          data: covariateValue === null ? 'NA' : covariateValue
        };
        return covariateColumn;
      }
    }

    function getAbsoluteColumnsToShow(rows) {
      var hasDichotomous = hasRowWithDataType(rows, 'dichotomous');
      var hasContinuous = hasRowWithDataType(rows, 'continuous');
      var hasSurvival = hasRowWithDataType(rows, 'survival');

      return {
        rate: (hasDichotomous || hasSurvival),
        mu: hasContinuous,
        sigma: hasContinuous ? shouldShowSigma(rows) : false,
        stdErr: hasContinuous ? shouldShowStandardError(rows) : false,
        sampleSize: hasContinuous || hasDichotomous ? shouldShowN(rows) : false,
        exposure: hasSurvival
      };
    }

    function hasRowWithDataType(rows, dataType) {
      return _.some(rows, function(row) {
        return _.some(row.measurements, ['type', dataType]);
      });
    }

    function shouldShowSigma(rows) {
      return _.some(rows, function(row) {
        return _.some(row.measurements, function(measurement) {
          return measurement.sigma !== 'NA';
        });
      });
    }

    function shouldShowN(rows) {
      return _.some(rows, function(row) {
        return _.some(row.measurements, function(measurement) {
          return measurement.sampleSize !== 'NA';
        });
      });
    }

    function shouldShowStandardError(rows) {
      return _.some(rows, function(row) {
        return _.some(row.measurements, function(measurement) {
          return measurement.stdErr !== 'NA' && measurement.stdErr !== null;
        });
      });
    }

    function getContrastColumnsToShow(rows) {
      return _.reduce(CONTRAST_RESULT_PROPERTIES, function(accum, property) {
        accum[property] = hasMeasurementType(rows, property);
        return accum;
      }, {});
    }

    function hasMeasurementType(rows, type) {
      return _.some(rows, function(row) {
        return _.some(row.measurements, function(measurement) {
          return hasValue(measurement[type]);
        });
      });
    }

    function getRowMeasurementType(measurement) {
      if (!measurement) {
        return 'unknown';
      }
      var type = measurement.measurementTypeURI;
      return type.slice(type.lastIndexOf('#') + 1); // strip of http://blabla/ontology#
    }

    function buildMissingValueByStudy(rows, momentSelections) {
      return _.reduce(rows, function(accum, row) {
        accum[row.studyUri] = accum[row.studyUri] || doesRowHaveMissingValues(row, momentSelections);
        return accum;
      }, {});
    }

    function doesRowHaveMissingValues(row, momentSelections) {
      var momentUri = momentSelections[row.studyUri].uri;
      return !isReferenceArm(row) && row.included && hasMissingValue(row.measurements[momentUri]);
    }

    function isReferenceArm(row) {
      return row.isContrastArm && !row.measurements;
    }

    function hasMissingValue(measurement) {
      if (measurement.referenceArm) {
        return hasMissingContrastValue(measurement);
      } else {
        return hasMissingAbsoluteValue(measurement);
      }
    }

    function hasMissingContrastValue(measurement) {
      return !hasValue(measurement.stdErr) || !hasValue(measurement.meanDifference);
    }

    function hasMissingAbsoluteValue(measurement) {
      var type = measurement.type || measurement.measurementTypeURI;
      switch (type) {
        case 'dichotomous':
          return !hasValue(measurement.rate) || !hasValue(measurement.sampleSize);
        case 'continuous':
          return !hasValue(measurement.mu) ||
            (!hasValue(measurement.sigma) && !hasValue(measurement.stdErr)) ||
            (!hasValue(measurement.sampleSize) && !hasValue(measurement.stdErr));
        case 'survival':
          return !hasValue(measurement.rate) || !hasValue(measurement.exposure);
        default:
          return true;
      }
    }

    function countMatchedInterventions(study, excludedArmsByUri) {
      var numberOfMatchedInterventions = 0;
      angular.forEach(study.arms, function(arm) {
        if (!excludedArmsByUri[arm.uri]) {
          numberOfMatchedInterventions += arm.matchedProjectInterventionIds.length;
        }
      });
      return numberOfMatchedInterventions;
    }

    function getValidStudies(studies, analysis) {
      var excludedArmsByUri = buildExcludedArmsMap(analysis.excludedArms);

      return _.filter(studies, function(study) {
        return countMatchedInterventions(study, excludedArmsByUri) > 1;
      });
    }

    function filterExcludedArms(studies, excludedArms) {
      var exclusionMap = buildExcludedArmsMap(excludedArms);
      return _.map(studies, function(study) {
        var copiedStudy = angular.copy(study);
        copiedStudy.arms = _.filter(study.arms, function(arm) {
          return !exclusionMap[arm.uri];
        });
        return copiedStudy;
      });
    }

    function sumInterventionSampleSizes(studies, intervention, analysis, momentSelections) {
      var interventionSum = _.reduce(studies, function(sum, study) {
        var selectedMM = momentSelections[study.studyUri];
        angular.forEach(study.arms, function(arm) {
          var matchedIntervention = _.find(arm.matchedProjectInterventionIds, function(id) {
            return id === intervention.id;
          });

          if (matchedIntervention) {
            var measurementForOutcome = getMeasurementForOutcome(analysis, arm, selectedMM);
            sum += measurementForOutcome ? measurementForOutcome.sampleSize : 0;
          }
        });
        return sum;
      }, 0);
      return interventionSum;
    }

    function transformStudiesToNetwork(studies, interventions, analysis, momentSelections) {
      var network = {
        interventions: [],
        edges: AnalysisService.generateEdges(interventions)
      };
      var validStudies = filterExcludedArms(studies, analysis.excludedArms);
      validStudies = getValidStudies(validStudies, analysis);

      network.interventions = _.map(interventions, function(intervention) {
        return {
          name: intervention.name,
          sampleSize: sumInterventionSampleSizes(validStudies, intervention, analysis, momentSelections)
        };
      });
      network.edges = attachStudiesForEdges(network.edges, validStudies);
      network.edges = _.filter(network.edges, function(edge) {
        return edge.studies.length > 0;
      });
      return network;
    }

    function attachStudiesForEdges(edges, studies) {
      return _.map(edges, function(edge) {
        edge.studies = _.filter(studies, function(study) {
          return studyMeasuresBothInterventions(study, edge.from, edge.to);
        });
        return edge;
      });
    }

    function studyMeasuresBothInterventions(study, fromIntervention, toIntervention) {
      return fromIntervention && toIntervention &&
        findArmForIntervention(study.arms, fromIntervention) &&
        findArmForIntervention(study.arms, toIntervention);
    }

    function findArmForIntervention(arms, trialDataIntervention) {
      return _.find(arms, function(arm) {
        return _.some(arm.matchedProjectInterventionIds, function(id) {
          return id === trialDataIntervention.id;
        });
      });
    }

    function buildInterventionInclusions(interventions, analysis) {
      return _.reduce(interventions, function(accum, intervention) {
        if (intervention.isIncluded) {
          accum.push({
            analysisId: analysis.id,
            interventionId: intervention.id
          });
        }
        return accum;
      }, []);
    }

    function doesModelHaveAmbiguousArms(studies, analysis) {
      return _.find(studies, function(study) {
        return _.some(analysis.interventionInclusions, function(inclusion) {
          return doesInterventionHaveAmbiguousArms(inclusion.interventionId, study.studyUri, studies, analysis);
        });
      });
    }

    function doesInterventionHaveAmbiguousArms(interventionId, studyUri, studies, analysis) {
      if (interventionId === null) {
        return false;
      }
      var containingStudy = _.find(studies, ['studyUri', studyUri]);
      var includedArmsForInterventionId = _.filter(containingStudy.arms, function(arm) {
        return _.some(arm.matchedProjectInterventionIds, function(id) {
          return interventionId === id;
        }) && isArmIncluded(analysis, arm);
      });
      return includedArmsForInterventionId.length > 1;
    }

    function isArmIncluded(analysis, arm) {
      return !_.some(analysis.excludedArms, ['trialverseUid', arm.uri]);
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

    function cleanUpExcludedArms(excludedIntervention, analysis, studies) {
      return _.reject(analysis.excludedArms, function(excludedArm) {
        return _.some(studies, function(study) {
          var armsMatchingExcludedIntervention = _.some(study.arms, function(arm) {
            return arm.uri === excludedArm.trialverseUid &&
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

    function buildOverlappingTreatmentMap(interventions, studies) {
      var includedInterventions = _.filter(interventions, 'isIncluded');
      var includedInterventionMap = _.keyBy(includedInterventions, 'id');
      var overlappingTreatmentsMap = {};

      studies.forEach(function(study) {
        study.arms.forEach(function(arm) {
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

    function doesModelHaveInsufficientCovariateValues(studies) {
      var covariateValues = _.reduce(studies, function(accum, study) {
        _.forEach(study.covariatesColumns, function(covariateColumn) {
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

    function changeArmExclusion(row, analysis) {
      if (row.included) {
        _.remove(analysis.excludedArms, function(arm) {
          return arm.trialverseUid === row.trialverseUid;
        });
      } else {
        analysis.excludedArms.push({
          analysisId: analysis.id,
          trialverseUid: row.trialverseUid
        });
      }
      return analysis;
    }

    function buildMomentSelections(studies, analysis) {
      return _.reduce(studies, function(accum, study) {
        var manualSelection = _.find(analysis.includedMeasurementMoments, function(selectedMM) {
          return selectedMM.study === study.studyUri;
        });
        var selectedMMUri = manualSelection ? manualSelection.measurementMoment : study.defaultMeasurementMoment;

        var selectedMM = _.find(study.measurementMoments, function(measurementMoment) {
          return measurementMoment.uri === selectedMMUri;
        });
        if (selectedMM) {
          selectedMM.isDefault = manualSelection ? false : true;
        } else {
          selectedMM = study.measurementMoments[0];
        }
        accum[study.studyUri] = selectedMM;
        return accum;
      }, {});
    }

    function hasInterventionOverlap(interventionOverlapMap) {
      var overlapCount = _.size(interventionOverlapMap);
      return overlapCount > 0;
    }

    function getReferenceArms(rows) {
      return _(rows).filter(function(row) {
        return row.referenceArm === row.trialverseUid;
      }).keyBy('trialverseUid').value();
    }

    function doesModelContainTooManyResultProperties(rows, momentSelections) {
      return doesModelContainTooManyContrastResultProperties(rows, momentSelections) ||
        doesModelContainTooManyAbsoluteResultProperties(rows, momentSelections);
    }

    function doesModelContainTooManyAbsoluteResultProperties(rows, momentSelections) {
      var propertiesInModel = getResultPropertiesInModel(rows, momentSelections, ABSOLUTE_RESULT_PROPERTIES);
      var numberofProperties = _(ABSOLUTE_RESULT_PROPERTIES).filter(function(property) {
        return propertiesInModel[property];
      });
      var dichotomousOrSurvival = propertiesInModel.rate &&
        (propertiesInModel.sampleSize || propertiesInModel.exposure) &&
        numberofProperties > 2;
      var continuous = propertiesInModel.mean &&
        (propertiesInModel.stdErr || (propertiesInModel.sampleSize && propertiesInModel.stdDev)) &&
        (propertiesInModel.rate || propertiesInModel.exposure);
      return dichotomousOrSurvival || continuous;
    }

    function doesModelContainTooManyContrastResultProperties(rows, momentSelections) {
      var propertiesInModel = getResultPropertiesInModel(rows, momentSelections, CONTRAST_RESULT_PROPERTIES);

      var numberOfProperties = _(CONTRAST_RESULT_PROPERTIES).filter(function(property) {
        return property !== 'stdErr' && propertiesInModel[property];
      }).size();
      return numberOfProperties > 1;
    }

    function getResultPropertiesInModel(rows, momentSelections, properties) {
      return _.reduce(properties, function(accum, property) {
        accum[property] = findPropertyInRows(rows, momentSelections, property);
        return accum;
      }, {});
    }

    function findPropertyInRows(rows, momentSelections, property) {
      return _.some(rows, function(row) {
        var moment = momentSelections[row.studyUri];
        return row.included && row.measurements && hasValue(row.measurements[moment.uri][property]);
      });
    }

    function hasMissingCovariateValues(tables) {
      return hasStudyWithMissingCovariateValues(tables.absolute) || hasStudyWithMissingCovariateValues(tables.contrast);
    }

    function hasStudyWithMissingCovariateValues(rows) {
      return _.some(rows, function(row) {
        return row.numberOfIncludedInterventions > 1 && _.some(row.covariatesColumns, function(covariate) {
          return covariate.data === 'NA' ;
        });
      });
    }

    return {
      addInclusionsToCovariates: addInclusionsToCovariates,
      addInclusionsToInterventions: addInclusionsToInterventions,
      buildInterventionInclusions: buildInterventionInclusions,
      buildMissingValueByStudy: buildMissingValueByStudy,
      buildMomentSelections: buildMomentSelections,
      buildOverlappingTreatmentMap: buildOverlappingTreatmentMap,
      changeArmExclusion: changeArmExclusion,
      changeCovariateInclusion: changeCovariateInclusion,
      getAbsoluteColumnsToShow: getAbsoluteColumnsToShow,
      getContrastColumnsToShow: getContrastColumnsToShow,
      cleanUpExcludedArms: cleanUpExcludedArms,
      doesInterventionHaveAmbiguousArms: doesInterventionHaveAmbiguousArms,
      doesModelHaveAmbiguousArms: doesModelHaveAmbiguousArms,
      doesModelHaveInsufficientCovariateValues: doesModelHaveInsufficientCovariateValues,
      getIncludedInterventions: getIncludedInterventions,
      transformStudiesToNetwork: transformStudiesToNetwork,
      transformStudiesToTableRows: transformStudiesToTableRows,
      hasInterventionOverlap: hasInterventionOverlap,
      getReferenceArms: getReferenceArms,
      doesModelContainTooManyResultProperties: doesModelContainTooManyResultProperties,
      hasMissingCovariateValues: hasMissingCovariateValues
    };
  };
  return dependencies.concat(NetworkMetaAnalysisService);
});
