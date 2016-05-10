'use strict';
define(['lodash', 'angular'], function(_, angular) {
  var dependencies = ['AnalysisService'];

  var NetworkMetaAnalysisService = function(AnalysisService) {

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
          row.drugConceptUid = trialDataArm.semanticIntervention.drugConcept;
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
          row.rate = outcomeMeasurement.rate;
          row.mu = outcomeMeasurement.mean;
          row.sigma = outcomeMeasurement.stdDev;
          row.sampleSize = outcomeMeasurement.sampleSize;
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

    function countMatchedInterventions(study) {
      var numberOfMatchedInterventions = 0;
      angular.forEach(study.trialDataArms, function(trialDataArm) {
        numberOfMatchedInterventions = numberOfMatchedInterventions + trialDataArm.matchedProjectInterventionIds.length;
      });
      return numberOfMatchedInterventions;
    }

    function filterStudiesHavingLessThanTwoMatchedInterventions(trialData) {
      return _.filter(trialData, function(study) {
        return countMatchedInterventions(study) > 1;
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
      validTrialData = filterStudiesHavingLessThanTwoMatchedInterventions(validTrialData);

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

    function isNetworkDisconnected(network) {
      var toVisit = [network.interventions[0]];
      var visited = [];

      function findEdgesConnectedToNode(node) {
        return _.filter(network.edges, function(edge) {
          return edge.from.name === node.name || edge.to.name === node.name;
        });
      }

      function addUnvisitedNodesToToVisitList(edge) {
        if (!_.find(visited, ['name', edge.to.name])) {
          toVisit.push(edge.to);
        } else if (!_.find(visited, ['name', edge.from.name])) {
          toVisit.push(edge.from);
        }
      }

      function areNodeSetsEqual(setA, setB) {
        var namesA = _.map(setA, 'name');
        var namesB = _.map(setB, 'name');
        return !_.difference(namesA, namesB).length;
      }

      if (!network.interventions.length) {
        return true;
      }

      while (toVisit.length) {
        var node = toVisit.pop();
        visited.push(node);
        var connectedEdges = findEdgesConnectedToNode(node);
        _.each(connectedEdges, addUnvisitedNodesToToVisitList);
      }
      return !areNodeSetsEqual(network.interventions, visited);
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

    function isArmIncluded(analysis, trialDataArm) {
      return !_.find(analysis.excludedArms, function(exclusion) {
        return exclusion.trialverseUid === trialDataArm.uri;
      });
    }

    function doesModelHaveAmbiguousArms(trialDataStudies, analysis) {
      function doesStudyHaveAmbiguousArms(trialDataStudy) {
        return _.find(trialDataStudy.trialDataArms, function(arm) {
          return (arm.matchedProjectInterventionIds.length > 1) && isArmIncluded(analysis, arm);
        });

      }

      return _.find(trialDataStudies, function(trialDataStudy) {
        return doesStudyHaveAmbiguousArms(trialDataStudy);
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


    function cleanUpExcludedArms(intervention, analysis, trialDataStudies) {

      var armsMatchingIntervention = {};

      angular.forEach(trialDataStudies, function(trialDataStudy) {
        var drugUidForInterventionInStudy;

        angular.forEach(trialDataStudy.trialDataArms, function(trialDataArm) {
          if (trialDataArm.semanticIntervention.drugConcept === intervention.semanticInterventionUri) {
            drugUidForInterventionInStudy = trialDataArm.semanticIntervention.drugConcept;
          }
        });

        if (drugUidForInterventionInStudy) {
          angular.forEach(trialDataStudy.trialDataArms, function(trialDataArm) {
            if (trialDataArm.semanticIntervention.drugConcept === drugUidForInterventionInStudy) {
              armsMatchingIntervention[trialDataArm.uri] = true;
            }
          });
        }
      });

      return _.filter(analysis.excludedArms, function(excludedArm) {
        return !armsMatchingIntervention[excludedArm.trialverseUid];
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
        if(!overlappingTreatmentsMap[interventionId]) {
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

    return {
      transformTrialDataToNetwork: transformTrialDataToNetwork,
      transformTrialDataToTableRows: transformTrialDataToTableRows,
      isNetworkDisconnected: isNetworkDisconnected,
      addInclusionsToInterventions: addInclusionsToInterventions,
      addInclusionsToCovariates: addInclusionsToCovariates,
      changeArmExclusion: changeArmExclusion,
      buildInterventionInclusions: buildInterventionInclusions,
      doesInterventionHaveAmbiguousArms: doesInterventionHaveAmbiguousArms,
      doesModelHaveAmbiguousArms: doesModelHaveAmbiguousArms,
      cleanUpExcludedArms: cleanUpExcludedArms,
      changeCovariateInclusion: changeCovariateInclusion,
      buildOverlappingTreatmentMap: buildOverlappingTreatmentMap,
      getIncludedInterventions: getIncludedInterventions
    };
  };
  return dependencies.concat(NetworkMetaAnalysisService);
});
