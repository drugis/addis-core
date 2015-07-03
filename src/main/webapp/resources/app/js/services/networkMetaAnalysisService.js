'use strict';
define(['angular'], function() {
  var dependencies = ['AnalysisService'];

  var NetworkMetaAnalysisService = function(AnalysisService) {

    function findInterventionOptionForDrug(drugInstanceUid, interventionOptions) {
      return _.find(interventionOptions, function(intervention) {
        return intervention.semanticInterventionUri === drugInstanceUid;
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

    function buildTableFromTrialData(data, interventions, excludedArms) {
      var rows = [];
      if (interventions.length < 1) {
        return rows;
      }
      var exclusionMap = buildExcludedArmsMap(excludedArms);
      angular.forEach(data.trialDataStudies, function(study) {
        var studyRows = [];
        angular.forEach(study.trialDataArms, function(trialDataArm) {
          var matchedIntervention = findInterventionOptionForDrug(trialDataArm.drugConceptUid, interventions);
          var row = {};

          row.study = study.name;
          row.studyUid = study.studyUid;
          row.studyRowSpan = study.trialDataArms.length;
          row.studyRows = studyRows;

          row.intervention = matchedIntervention ? matchedIntervention.semanticInterventionLabel : 'unmatched';
          row.drugInstanceUid = trialDataArm.drugInstanceUid;
          row.drugConceptUid = trialDataArm.drugConceptUid;
          row.arm = trialDataArm.name;
          row.trialverseUid = trialDataArm.uid;
          row.included = !exclusionMap[trialDataArm.uid] && row.intervention !== 'unmatched';

          row.rate = trialDataArm.measurement.rate;
          row.mu = trialDataArm.measurement.mean;
          row.sigma = trialDataArm.measurement.stdDev;
          row.sampleSize = trialDataArm.measurement.sampleSize;

          rows.push(row);
          studyRows.push(row);
        });
      });
      return rows;
    }

    function isMatchedTrialDataIntervention(trialDataIntervention, study) {
      return _.find(study.trialDataArms, function(trialDataArm) {
        return trialDataIntervention.drugInstanceUid === trialDataArm.drugInstanceUid;
      });
    }

    function countMatchedInterventions(study) {
      var numberOfMatchedInterventions = 0;
      angular.forEach(study.trialDataInterventions, function(trialDataIntervention) {
        if (isMatchedTrialDataIntervention(trialDataIntervention, study)) {
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
          return !exclusionMap[arm.uid];
        });
        return copiedStudy;
      });
    }

    function sumInterventionSampleSizes(trialData, intervention) {
      var interventionSum = _.reduce(trialData, function(sum, trialDataStudy) {
        angular.forEach(trialDataStudy.trialDataArms, function(trialDataArm) {

          if (trialDataArm.drugConceptUid === intervention.semanticInterventionUri) {
            sum += trialDataArm.measurement.sampleSize;
          }
        });
        return sum;
      }, 0);
      return interventionSum;
    }



    function findArmForIntervention(trialdataArms, trialDataIntervention) {
      return _.find(trialdataArms, function(trialdataArm) {
        return trialdataArm.drugInstanceUid === trialDataIntervention.drugInstanceUid;
      });
    }

    function findTrialDataInterventionForIntervention(trialDataInterventions, intervention) {
      return _.find(trialDataInterventions, function(trialDataIntervention) {
        return trialDataIntervention.drugConceptUid === intervention.semanticInterventionUri;
      });
    }

    function studyMeasuresBothInterventions(trialDataStudy, intervention1, intervention2) {
      var trialDataIntervention1 = findTrialDataInterventionForIntervention(trialDataStudy.trialDataInterventions, intervention1);
      var trialDataIntervention2 = findTrialDataInterventionForIntervention(trialDataStudy.trialDataInterventions, intervention2);
      return trialDataIntervention1 && trialDataIntervention2 &&
        findArmForIntervention(trialDataStudy.trialDataArms, trialDataIntervention1) &&
        findArmForIntervention(trialDataStudy.trialDataArms, trialDataIntervention2);
    }

    function attachStudiesForEdges(edges, trialData) {
      return _.map(edges, function(edge) {
        var studiesMeasuringEdge = _.filter(trialData, function(trialDataStudy) {
          return studyMeasuresBothInterventions(trialDataStudy, edge.from, edge.to);
        });
        edge.numberOfStudies = studiesMeasuringEdge ? studiesMeasuringEdge.length : 0;
        return edge;
      });
    }

    function transformTrialDataToNetwork(trialData, interventions, excludedArms) {
      var network = {
        interventions: [],
        edges: AnalysisService.generateEdges(interventions)
      };
      var validTrialData = filterExcludedArms(trialData.trialDataStudies, excludedArms);
      validTrialData = filterStudiesHavingLessThanTwoMatchedInterventions(validTrialData);

      network.interventions = _.map(interventions, function(intervention) {
        return {
          name: intervention.name,
          sampleSize: sumInterventionSampleSizes(validTrialData, intervention)
        };
      });
      network.edges = attachStudiesForEdges(network.edges, validTrialData);
      network.edges = _.filter(network.edges, function(edge) {
        return edge.numberOfStudies > 0;
      });
      return network;
    }

    function transformTrialDataToTableRows(trialData, interventions, excludedArms) {
      var tableRows = buildTableFromTrialData(trialData, interventions, excludedArms);
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
        if (!_.findWhere(visited, {
          name: edge.to.name
        })) {
          toVisit.push(edge.to);
        } else if (!_.findWhere(visited, {
          name: edge.from.name
        })) {
          toVisit.push(edge.from);
        }
      }

      function areNodeSetsEqual(setA, setB) {
        var namesA = _.pluck(setA, 'name');
        var namesB = _.pluck(setB, 'name');
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
          trialverseUid: dataRow.trialverseUid
        });
      }
      return analysis;
    }

    function buildInterventionInclusions(interventions) {
      return _.reduce(interventions, function(accumulator, intervention) {
        if (intervention.isIncluded) {
          accumulator.push({
            interventionId: intervention.id
          });
        }
        return accumulator;
      }, []);
    }

    function doesModelHaveAmbiguousArms(trialverseData, interventions, analysis) {
      var includedInterventionUris = _.reduce(interventions, function(mem, intervention) {
        if (intervention.isIncluded) {
          mem = mem.concat(intervention.semanticInterventionUri);
        }
        return mem;
      }, []);

      function doesStudyHaveAmbiguousArms(trialDataStudy, includedInterventionUris) {
        return _.find(includedInterventionUris, function(includedInterventionUri) {
          var matchedInterventionsForInclusion = findMatchedArmsForIntervention(trialDataStudy.trialDataArms, includedInterventionUri);
          return matchedInterventionsForInclusion.length > 1;
        });
      }

      function findMatchedArmsForIntervention(trialDataArms, includedInterventionUri) {
        return _.filter(trialDataArms, function(trialDataArm) {
          return trialDataArm.drugConceptUid === includedInterventionUri && isArmIncluded(trialDataArm);
        });
      }

      function isArmIncluded(trialDataArm) {
        return !_.find(analysis.excludedArms, function(exclusion) {
          return exclusion.trialverseUid === trialDataArm.uid;
        });
      }

      return _.find(trialverseData.trialDataStudies, function(trialDataStudy) {
        return doesStudyHaveAmbiguousArms(trialDataStudy, includedInterventionUris);
      });
    }

    function doesInterventionHaveAmbiguousArms(drugConceptUid, studyUid, trialverseData, analysis) {
      function isArmIncluded(trialDataArm) {
        return !_.find(analysis.excludedArms, function(exclusion) {
          return exclusion.trialverseUid === trialDataArm.uid;
        });
      }
      var containingStudy = _.find(trialverseData.trialDataStudies, function(trialDataStudy) {
        return trialDataStudy.studyUid === studyUid;
      });
      var includedArmsForDrugUid = _.filter(containingStudy.trialDataArms, function(trialDataArm) {
        return trialDataArm.drugConceptUid === drugConceptUid && isArmIncluded(trialDataArm);
      });

      return includedArmsForDrugUid.length > 1;
    }

    function addInclusionsToInterventions(interventions, inclusions) {
      var inclusionMap = _.object(_.map(inclusions, function(inclusion) {
        return [inclusion.interventionId, true];
      }));

      angular.forEach(interventions, function(intervention) {
        intervention.isIncluded = inclusionMap[intervention.id];
      });
      return interventions;
    }

    function cleanUpExcludedArms(intervention, analysis, trialverseData) {

      var armsMatchingIntervention = {};

      angular.forEach(trialverseData.trialDataStudies, function(trialDataStudy) {
        var drugUidForInterventionInStudy;

        angular.forEach(trialDataStudy.trialDataInterventions, function(trialDataIntervention) {
          if (trialDataIntervention.drugConceptUid === intervention.semanticInterventionUri) {
            drugUidForInterventionInStudy = trialDataIntervention.drugConceptUid;
          }
        });

        if (drugUidForInterventionInStudy) {
          angular.forEach(trialDataStudy.trialDataArms, function(trialDataArm) {
            if (trialDataArm.drugConceptUid === drugUidForInterventionInStudy) {
              armsMatchingIntervention[trialDataArm.id] = true;
            }
          });
        }
      });

      return _.filter(analysis.excludedArms, function(excludedArm) {
        return !armsMatchingIntervention[excludedArm.trialverseUid];
      });

    }

    return {
      transformTrialDataToNetwork: transformTrialDataToNetwork,
      transformTrialDataToTableRows: transformTrialDataToTableRows,
      isNetworkDisconnected: isNetworkDisconnected,
      addInclusionsToInterventions: addInclusionsToInterventions,
      changeArmExclusion: changeArmExclusion,
      buildInterventionInclusions: buildInterventionInclusions,
      doesInterventionHaveAmbiguousArms: doesInterventionHaveAmbiguousArms,
      doesModelHaveAmbiguousArms: doesModelHaveAmbiguousArms,
      cleanUpExcludedArms: cleanUpExcludedArms
    };
  };
  return dependencies.concat(NetworkMetaAnalysisService);
});
