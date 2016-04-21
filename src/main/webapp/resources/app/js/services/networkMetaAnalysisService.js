'use strict';
define(['lodash', 'angular'], function(_, angular) {
  var dependencies = ['AnalysisService'];

  var NetworkMetaAnalysisService = function(AnalysisService) {

    function findInterventionOptionForDrug(drugInstanceUri, interventionOptions) {
      return _.find(interventionOptions, function(intervention) {
        return intervention.semanticInterventionUri === drugInstanceUri;
      });
    }

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


          if (trialDataArm.matchedProjectInterventionId !== null) {
            var intervention = _.find(interventions, function(intervention) {
              return trialDataArm.matchedProjectInterventionId === intervention.id;
            });
            row.intervention = intervention.name;
            row.interventionId = intervention.id;
            ++numberOfMatchedInterventions;
          } else {
            row.intervention = 'unmatched';
          }

          row.included = !exclusionMap[trialDataArm.uri] && row.intervention !== 'unmatched';
          if(row.included){
            ++numberOfIncludedInterventions;
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
        if (trialDataArm.matchedProjectInterventionId !== null) {
          ++numberOfMatchedInterventions;
        }
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
          if (trialDataArm.matchedProjectInterventionId === intervention.id) {
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
        return trialdataArm.matchedProjectInterventionId === trialDataIntervention.id;
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
        var interventionCounts = trialDataStudy.trialDataArms.reduce(function(acc, arm) {
          if (arm.matchedProjectInterventionId !== null && isArmIncluded(analysis, arm)) {
            if (acc[arm.matchedProjectInterventionId]) {
              ++acc[arm.matchedProjectInterventionId];
            } else {
              acc[arm.matchedProjectInterventionId] = 1;
            }
          }
          return acc;
        }, {});
        return _.find(interventionCounts, function(count) {
          return count > 1;
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
        return trialDataArm.matchedProjectInterventionId === interventionId && isArmIncluded(trialDataArm);
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

        angular.forEach(trialDataStudy.trialDataInterventions, function(trialDataIntervention) {
          if (trialDataIntervention.drugConceptUid === intervention.semanticInterventionUri) {
            drugUidForInterventionInStudy = trialDataIntervention.semanticIntervention.drugConcept;
          }
        });

        if (drugUidForInterventionInStudy) {
          angular.forEach(trialDataStudy.trialDataArms, function(trialDataArm) {
            if (trialDataArm.semanticIntervention.drugConcept === drugUidForInterventionInStudy) {
              armsMatchingIntervention[trialDataArm.id] = true;
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

    function findOverlappingTreatments(thisIntervention, interventions) {
      return interventions.reduce(function(overlapping, otherIntervention) {
        if (thisIntervention.id !== otherIntervention.id &&
          thisIntervention.semanticInterventionUri === otherIntervention.semanticInterventionUri) {
          overlapping.push(otherIntervention);
        }
        return overlapping;
      }, []);
    }

    function getIncludedInterventions(interventions) {
      return _.filter(interventions, function(intervention) {
        return intervention.isIncluded;
      });
    }

    function hasMoreThanOneIncludedIntervention(study, includedInterventions) {
      var numberOfIncludedInterventions = study.trialDataArms.reduce(function(count, arm) {
        var intervention = findInterventionOptionForDrug(arm.drugConceptUid, includedInterventions);
        if (intervention && intervention.isIncluded) {
          ++count;
        }
        return count;
      }, 0);

      return numberOfIncludedInterventions > 1;
    }

    function buildOverlappingTreatmentMap(analysis, interventions, trialDataStudies) {
      var includedInterventions = getIncludedInterventions(interventions);
      var overlappingTreatmentsMap = includedInterventions.reduce(function(accum, intervention) {
        var overlapping = findOverlappingTreatments(intervention, includedInterventions);
        if (overlapping.length > 0) {
          accum[intervention.id] = overlapping;
        }
        return accum;
      }, {});

      return _.reduce(overlappingTreatmentsMap, function(accum, overlapEntry, key) {
        var intervention = includedInterventions.find(function(intervention) {
          return intervention.id === parseInt(key);
        });

        var hasOverlap = _.find(trialDataStudies, function(study) {

          var includedArms = _.filter(study.trialDataArms, function(arm) {
            return !_.find(analysis.excludedArms, function(exclusion) {
              return exclusion.trialverseUid === arm.uri;
            });
          });

          var hasIncludedArmWithOverlap = _.find(includedArms, function(arm) {
            return intervention.semanticInterventionUri === arm.drugConceptUid;
          });

          var moreThanOneIncludedIntervention = hasMoreThanOneIncludedIntervention(study, includedInterventions);

          return moreThanOneIncludedIntervention && hasIncludedArmWithOverlap;
        });
        if (hasOverlap) {
          accum[intervention.id] = overlapEntry;
        }
        return accum;
      }, {});
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
      buildOverlappingTreatmentMap: buildOverlappingTreatmentMap
    };
  };
  return dependencies.concat(NetworkMetaAnalysisService);
});
