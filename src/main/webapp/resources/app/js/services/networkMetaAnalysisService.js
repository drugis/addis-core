'use strict';
define(['angular'], function() {
  var dependencies = [];
  var interventionMap = {};

  var NetworkMetaAnalysisService = function() {

    function resolveInterventionName(trialDataIntervention, interventionOptions) {
      return _.find(interventionOptions, function(intervention) {
        return intervention.semanticInterventionUri === trialDataIntervention.uri;
      }).name;
    }

    function mapTrialDataArmToIntervention(trialDataArm, trialDataInterventions) {
      return interventionMap[trialDataArm.drugId] ? interventionMap[trialDataArm.drugId] : _.find(trialDataInterventions, function(trialDataIntervention) {
        return trialDataIntervention.drugId === trialDataArm.drugId;
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

    function addRenderingHintsToTable(table) {
      // add information to render the table
      var currentStudy = 'null';
      var currentInterventionRow = {
        intervention: null
      };
      angular.forEach(table, function(row) {

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
      });
      return table;
    }

    function buildTableFromTrialData(data, interventions) {
      var rows = [];
      angular.forEach(data.trialDataStudies, function(study) {
        angular.forEach(study.trialDataArms, function(trialDataArm) {
          var trialDataIntervention = mapTrialDataArmToIntervention(trialDataArm, study.trialDataInterventions);
          var row = {};
          row.study = study.name;
          row.studyRowSpan = study.trialDataArms.length;
          row.intervention = trialDataIntervention ? resolveInterventionName(trialDataIntervention, interventions) : 'unmatched';
          row.arm = trialDataArm.name;
          row.rate = findMeasurementValue(trialDataArm.measurements, 'rate', 'integerValue');
          row.mu = findMeasurementValue(trialDataArm.measurements, 'mean', 'realValue');
          row.sigma = findMeasurementValue(trialDataArm.measurements, 'standard deviation', 'realValue');
          row.sampleSize = findMeasurementValue(trialDataArm.measurements, 'sample size', 'integerValue');

          rows.push(row);
        });
      });
      return rows;
    }

    function countMatchedArms(study) {
      return _.filter(study.trialDataArms, function(trialdataArm) {
        return mapTrialDataArmToIntervention(trialdataArm, study.trialDataInterventions);
      }).length;
    }

    function filterOneArmStudies(trialData) {
      return _.filter(trialData, function(study) {
        return countMatchedArms(study) > 1;
      });
    }


    function sumInterventionSampleSizes(trialData, intervention) {
      var interventionSum = _.reduce(trialData, function(sum, trialDataStudy) {
        angular.forEach(trialDataStudy.trialDataArms, function(trialDataArm) {
          var trialDataIntervention = mapTrialDataArmToIntervention(trialDataArm, trialDataStudy.trialDataInterventions);
          if (trialDataIntervention && trialDataIntervention.uri === intervention.semanticInterventionUri) {
            sum += findMeasurementValue(trialDataArm.measurements, 'sample size', 'integerValue');
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
        return trialdataArm.drugId === trialDataIntervention.drugId;
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

    function transformTrialDataToNetwork(trialData, interventions) {
      var validTrialData = filterOneArmStudies(trialData.trialDataStudies);

      var network = {
        interventions: [],
        edges: generateEdges(interventions)
      };
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

    function transformTrialDataToTableRows(trialData, interventions) {
      var tableRows = buildTableFromTrialData(trialData, interventions);
      tableRows = sortTableByStudyAndIntervention(tableRows);
      tableRows = addRenderingHintsToTable(tableRows);
      return tableRows;
    }

    function isNetworkDisconnected(network) {

      function findEdgesStartingInNode(node) {
        return _.filter(network.edges, function(edge) {
          return edge.from.name === node.name;
        });
      }

      function addUnvisitedNodesToToVisitList(edge) {
        if (!_.contains(visited, edge.to)) {
          toVisit.push(edge.to);
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
      var toVisit = [network.interventions[0]];
      var visited = [];
      while (toVisit.length) {
        var node = toVisit.pop();
        visited.push(node);
        _.each(findEdgesStartingInNode(node), addUnvisitedNodesToToVisitList);
      }
      return !areNodeSetsEqual(network.interventions, visited);

    }

    return {
      transformTrialDataToNetwork: transformTrialDataToNetwork,
      transformTrialDataToTableRows: transformTrialDataToTableRows,
      isNetworkDisconnected: isNetworkDisconnected
    };
  };
  return dependencies.concat(NetworkMetaAnalysisService);
});