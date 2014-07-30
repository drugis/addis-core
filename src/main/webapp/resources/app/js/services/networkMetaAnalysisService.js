'use strict';
define(['angular'], function() {
  var dependencies = [];
  var interventionMap = {};

  var NetworkMetaAnalysisService = function() {

    function findInterventionOptionForDrug(drugInstanceUid, interventionOptions) {
      return _.find(interventionOptions, function(intervention) {
        return intervention.semanticInterventionUri === drugInstanceUid;
      });
    }

    function mapTrialDataArmToIntervention(trialDataArm, trialDataInterventions) {
      return interventionMap[trialDataArm.drugUid] ? interventionMap[trialDataArm.drugUid] : _.find(trialDataInterventions, function(trialDataIntervention) {
        return trialDataIntervention.drugUid === trialDataArm.drugUid;
      });
    }

    function findMeasurementValue(measurements, measurementAttribute, valueType) {
      var measurement = _.find(measurements, function(measurement) {
        return measurement.measurementAttribute === measurementAttribute;
      });
      return measurement !== undefined ? measurement[valueType] : null;
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
        exclusions[excludedArm.trialverseId] = true;
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
          var drugInstanceUid = trialDataArm.drugInstanceUid;
          var matchedIntervention = findInterventionOptionForDrug(trialDataArm.drugUid, interventions);
          var row = {};

          row.study = study.name;
          row.studyRowSpan = study.trialDataArms.length;
          row.studyRows = studyRows;

          row.intervention = matchedIntervention ? matchedIntervention.semanticInterventionLabel : 'unmatched';
          row.drugUid = trialDataArm.drugUid;
          row.arm = trialDataArm.name;
          row.trialverseId = trialDataArm.uid;
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
        return trialDataIntervention.drugUid === trialDataArm.drugInstanceUid;
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
          
          if (trialDataArm.drugUid === intervention.semanticInterventionUri) {
            sum += trialDataArm.measurement.sampleSize;
          }
        });
        return sum;
      }, 0);
      return interventionSum;
    }

    function generateEdges(interventions) {
      var edges = [];
      _.each(interventions, function(rowIntervention, index) {
        var rest = interventions.slice(index + 1, interventions.length);
        _.each(rest, function(colIntervention) {
          edges.push({
            from: rowIntervention,
            to: colIntervention
          });
        });
      });

      return edges;
    }

    function findArmForIntervention(trialdataArms, trialDataIntervention) {
      return _.find(trialdataArms, function(trialdataArm) {
        return trialdataArm.drugInstanceUid === trialDataIntervention.drugUid;
      });
    }

    function findTrialDataInterventionForIntervention(trialDataInterventions, intervention) {
      return _.find(trialDataInterventions, function(trialDataIntervention) {
        return trialDataIntervention.uri === intervention.semanticInterventionUri;
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
        edges: generateEdges(interventions)
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
          if (analysis.excludedArms[i].trialverseId === dataRow.trialverseId) {
            analysis.excludedArms.splice(i, 1);
            break;
          }
        }
      } else {
        analysis.excludedArms.push({
          trialverseId: dataRow.trialverseId
        });
      }
      return analysis;
    }

    function buildInterventionInclusions(interventions, analysis) {
      return _.reduce(interventions, function(accumulator, intervention) {
        if (intervention.isIncluded) {
          accumulator.push({
            interventionId: intervention.id
          });
        }
        return accumulator;
      }, []);
    }

    function doesModelHaveAmbiguousArms(trialverseData, analysis) {
      var hasAmbiguousArms = false;
      var drugUidSet = {};
      angular.forEach(trialverseData.trialDataStudies, function(trialDataStudy) {
        angular.forEach(trialDataStudy.trialDataArms, function(trialDataArm) {
          drugUidSet[trialDataArm.drugUid] = true;
        });
      });

      angular.forEach(_.map(_.keys(drugUidSet), Number), function(drugUid) {
        hasAmbiguousArms = hasAmbiguousArms || doesInterventionHaveAmbiguousArms(drugUid, trialverseData, analysis);
      });

      return hasAmbiguousArms;
    }

    function doesInterventionHaveAmbiguousArms(drugUid, trialverseData, analysis) {
      var includedArmsForDrugUid = _.reduce(trialverseData.trialDataStudies, function(arms, trialDataStudy) {
        return arms.concat(_.filter(trialDataStudy.trialDataArms, function(trialDataArm) {
          return trialDataArm.drugUid === drugUid && isArmIncluded(trialDataArm) && isMatched(trialDataStudy, trialDataArm);
        }));
      }, []);

      function isMatched(trialDataStudy, trialDataArm) {
        return _.find(trialDataStudy.trialDataInterventions, function(intervention) {
          return intervention.drugUid === trialDataArm.drugUid;
        });
      }

      function isArmIncluded(trialDataArm) {
        return !_.find(analysis.excludedArms, function(exclusion) {
          return exclusion.trialverseId === trialDataArm.id;
        });
      }
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
          if (trialDataIntervention.uri === intervention.semanticInterventionUri) {
            drugUidForInterventionInStudy = trialDataIntervention.drugUid;
          }
        });

        if (drugUidForInterventionInStudy) {
          angular.forEach(trialDataStudy.trialDataArms, function(trialDataArm) {
            if (trialDataArm.drugUid === drugUidForInterventionInStudy) {
              armsMatchingIntervention[trialDataArm.id] = true;
            }
          });
        }
      });

      return _.filter(analysis.excludedArms, function(excludedArm) {
        return !armsMatchingIntervention[excludedArm.trialverseId];
      });

    }

    function updateDataRowStudy(dataRow) {

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