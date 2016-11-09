'use strict';
define(['lodash', 'angular'], function(_, angular) {
  var dependencies = ['$filter', 'AnalysisService'];

  var NetworkMetaAnalysisService = function($filter, AnalysisService) {

    var CONTINUOUS_TYPE = 'http://trials.drugis.org/ontology#continuous';
    var DICHOTOMOUS_TYPE = 'http://trials.drugis.org/ontology#dichotomous';
    var CATEGORICAL_TYPE = 'http://trials.drugis.org/ontology#categorical';

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
        row;

      for (var i = 0; i < table.length; i++) {
        row = table[i];
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
        table[i] = row;
      }

      return table;
    }

    function buildExcludedArmsMap(excludedArms) {
      return _.reduce(excludedArms, function(exclusions, excludedArm) {
        exclusions[excludedArm.trialverseUid] = true;
        return exclusions;
      }, {});
    }

    function getOutcomeMeasurement(analysis, trialDataArm) {
      return _.find(trialDataArm.measurements, function(measurement) {
        return analysis.outcome.semanticOutcomeUri === measurement.variableConceptUri;
      });
    }

    function buildTableFromTrialData(trialDataStudies, interventions, analysis, covariates, treatmentOverlapMap) {
      var rows = [];
      if (interventions.length < 1) {
        return rows;
      }
      var exclusionMap = buildExcludedArmsMap(analysis.excludedArms);
      angular.forEach(trialDataStudies, function(study) {

        var numberOfMatchedInterventions = 0;
        var numberOfIncludedInterventions = 0;
        var studyRows = [];

        angular.forEach(study.trialDataArms, function(trialDataArm) {
          var row = {};
          row.covariatesColumns = [];
          row.study = study.name;
          row.studyUri = study.studyUri;
          row.studyUid = row.studyUri.slice(row.studyUri.lastIndexOf('/') + 1);
          row.studyRowSpan = study.trialDataArms.length;
          angular.forEach(covariates, function(covariate) {
            if (covariate.isIncluded) {
              var covariateValue = _.find(study.covariateValues, function(covariateValue) {
                return covariateValue.covariateKey === covariate.definitionKey;
              }).value;
              var covariateColumn = {
                headerTitle: covariate.name,
                data: covariateValue === null ? 'NA' : covariateValue
              };
              row.covariatesColumns.push(covariateColumn);
            }
          });
          row.arm = trialDataArm.name;
          row.trialverseUid = trialDataArm.uri;

          var overlappingTreatments;
          var intervention = _.find(interventions, function(intervention) {
            return _.find(trialDataArm.matchedProjectInterventionIds, function(id) {
              return id === intervention.id;
            });
          });

          if (intervention) {
            overlappingTreatments = treatmentOverlapMap[intervention.id];
            row.intervention = intervention.name;
            row.interventionId = intervention.id;
            ++numberOfMatchedInterventions;
          } else {
            row.intervention = 'unmatched';
          }

          row.included = !exclusionMap[trialDataArm.uri] && row.intervention !== 'unmatched';
          if (row.included) {
            ++numberOfIncludedInterventions;
          }

          if (row.included && overlappingTreatments) {
            overlappingTreatments = [intervention].concat(overlappingTreatments);
            row.overlappingInterventionWarning = _.map(overlappingTreatments, 'name').join(', ');
          }

          var outcomeMeasurement = getOutcomeMeasurement(analysis, trialDataArm);
          row.measurementType = getRowMeasurementType(outcomeMeasurement);
          row.rate = toTableLabel(outcomeMeasurement, 'rate');
          row.mu = toTableLabel(outcomeMeasurement, 'mean');
          var sigma = toTableLabel(outcomeMeasurement, 'stdDev');
          if (sigma !== 'NA') {
            sigma = $filter('number')(sigma, 3);
          }
          row.sigma = sigma;
          row.sampleSize = toTableLabel(outcomeMeasurement, 'sampleSize');
          studyRows.push(row);
        });
        studyRows = studyRows.map(function(studyRow) {
          studyRow.numberOfMatchedInterventions = numberOfMatchedInterventions;
          studyRow.numberOfIncludedInterventions = numberOfIncludedInterventions;
          return studyRow;
        });

        rows = rows.concat(studyRows);

      });
      return rows;
    }

    function getRowMeasurementType(measurement) {
      var type = measurement.measurementTypeURI;
      return type.slice(type.lastIndexOf('#') + 1); // strip of http://blabla/ontology#
    }

    function toTableLabel(measurement, field) {
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
        if (field === 'mean' || field === 'stdDev' || field === 'sampleSize') {
          return true;
        }
      }
      if (type === DICHOTOMOUS_TYPE) {
        if (field === 'rate' || field === 'sampleSize') {
          return true;
        }
      }
      if (type === CATEGORICAL_TYPE) {
        return false;
      }
      return false;
    }

    function buildMissingValueByStudyMap(trialDataStudies, analysis) {
      // setup maps
      var excludedArmsByUri = buildExcludedArmsMap(analysis.excludedArms);

      // look for single missingValue
      return _.reduce(trialDataStudies, function(accum, trialDataStudy) {
        var nMatchedInterventions = countMatchedInterventions(trialDataStudy, excludedArmsByUri);
        if (nMatchedInterventions < 2) { // if there's not enough matched interventions we don't care
          accum[trialDataStudy.studyUri] = false;
          return accum;
        }
        accum[trialDataStudy.studyUri] = _.find(trialDataStudy.trialDataArms, function(trialDataArm) {
          if (excludedArmsByUri[trialDataArm.uri]) {
            return false; //excluded arm, therefore missing values don't count
          }
          if (trialDataArm.matchedProjectInterventionIds.length < 1) {
            return false; //no matched interventions, therefore missing values don't count
          }
          var measurement = getOutcomeMeasurement(analysis, trialDataArm);
          var measurementType = measurement.measurementTypeURI;
          return isMissingValue(measurement.mean, 'mean', measurementType) ||
            isMissingValue(measurement.stdDev, 'stdDev', measurementType) ||
            isMissingValue(measurement.rate, 'rate', measurementType) ||
            isMissingValue(measurement.sampleSize, 'sampleSize', measurementType);
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

    function sumInterventionSampleSizes(trialData, intervention, analysis) {
      var interventionSum = _.reduce(trialData, function(sum, trialDataStudy) {
        angular.forEach(trialDataStudy.trialDataArms, function(trialDataArm) {

          var matchedIntervention = _.find(trialDataArm.matchedProjectInterventionIds, function(id) {
            return id === intervention.id;
          });

          if (matchedIntervention) {
            var outcomeMeasurement = getOutcomeMeasurement(analysis, trialDataArm);
            sum += outcomeMeasurement.sampleSize;
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


    function transformTrialDataToNetwork(trialDataStudies, interventions, analysis) {
      var network = {
        interventions: [],
        edges: AnalysisService.generateEdges(interventions)
      };
      var validTrialData = filterExcludedArms(trialDataStudies, analysis.excludedArms);
      validTrialData = getValidStudies(validTrialData, analysis);

      network.interventions = _.map(interventions, function(intervention) {
        return {
          name: intervention.name,
          sampleSize: sumInterventionSampleSizes(validTrialData, intervention, analysis)
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
        intervention.isIncluded = inclusionMap[intervention.id];
        return intervention;
      });
    }

    function addInclusionsToCovariates(covariates, inclusions) {
      var inclusionMap = _.fromPairs(_.map(inclusions, function(inclusion) {
        return [inclusion.covariateId, true];
      }));

      return covariates.map(function(covariate) {
        covariate.isIncluded = inclusionMap[covariate.id];
        return covariate;
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

    return {
      transformTrialDataToNetwork: transformTrialDataToNetwork,
      transformTrialDataToTableRows: transformTrialDataToTableRows,
      addInclusionsToInterventions: addInclusionsToInterventions,
      addInclusionsToCovariates: addInclusionsToCovariates,
      changeArmExclusion: changeArmExclusion,
      buildInterventionInclusions: buildInterventionInclusions,
      doesInterventionHaveAmbiguousArms: doesInterventionHaveAmbiguousArms,
      doesModelHaveAmbiguousArms: doesModelHaveAmbiguousArms,
      cleanUpExcludedArms: cleanUpExcludedArms,
      changeCovariateInclusion: changeCovariateInclusion,
      buildOverlappingTreatmentMap: buildOverlappingTreatmentMap,
      getIncludedInterventions: getIncludedInterventions,
      doesModelHaveInsufficientCovariateValues: doesModelHaveInsufficientCovariateValues,
      buildMissingValueByStudyMap: buildMissingValueByStudyMap
    };
  };
  return dependencies.concat(NetworkMetaAnalysisService);
});
