'use strict';
define(['angular', 'lodash'], function(angular, _) {
  var dependencies = ['StudyService', 'UUIDService'];
  var ResultsService = function(StudyService, UUIDService) {

    function updateResultValue(row, inputColumn) {
      return StudyService.getJsonGraph().then(function(graph) {
        if (!row.uri) {
          if (inputColumn.value) {
            // create branch
            var addItem = {
              '@id': 'http://trials.drugis.org/instances/' + UUIDService.generate(),
              of_group: row.group.armURI || row.group.groupUri,
              of_moment: row.measurementMoment.uri,
              of_outcome: row.variable.uri
            };
            addItem[inputColumn.valueName] = inputColumn.value;
            StudyService.saveJsonGraph(graph.concat(addItem));

            return addItem['@id'];
          } else {
            return undefined;
          }
        } else {
          // update branch
          var editItem = _.remove(graph, function(node) {
            return row.uri === node['@id'];
          })[0];

          if (inputColumn.value === null) {
            delete editItem[inputColumn.valueName];
          } else {
            editItem[inputColumn.valueName] = inputColumn.value;
          }

          if (!isEmptyResult(editItem)) {
            graph.push(editItem);
          } else {
            row.uri = undefined;
          }

          StudyService.saveJsonGraph(graph);
          return row.uri;
        }
      });
    }

    function isEmptyResult(item) {
      return !item.sample_size && !item.count && !item.standard_deviation && !item.mean;
    }

    function createValueItem(baseItem, backEndItem, type) {
      var valueItem = angular.copy(baseItem);
      valueItem.result_property = type;
      valueItem.value = backEndItem[type];
      return valueItem;
    }

    function toFrontend(accum, backEndItem) {
      // ?instance ?armUri ?momentUri ?result_property ?value
      var baseItem = {
        instance: backEndItem['@id'],
        armUri: backEndItem.of_group,
        momentUri: backEndItem.of_moment,
        outcomeUri: backEndItem.of_outcome,
      };

      if (isNonConformantMeasurementResult(backEndItem)) {
        baseItem.comment = backEndItem.comment;
      }

      if (backEndItem.sample_size !== undefined) {
        accum.push(createValueItem(baseItem, backEndItem, 'sample_size'));
      }

      if (backEndItem.count !== undefined) {
        accum.push(createValueItem(baseItem, backEndItem, 'count'));
      }

      if (backEndItem.standard_deviation !== undefined) {
        accum.push(createValueItem(baseItem, backEndItem, 'standard_deviation'));
      }

      if (backEndItem.mean !== undefined) {
        accum.push(createValueItem(baseItem, backEndItem, 'mean'));
      }

      return accum;
    }

    function isResultForVariable(variableUri, item) {
      return isResult(item) && variableUri === item.of_outcome;
    }

    function isResultForNonConformantMeasurement(variableUri, item) {
      return isNonConformantMeasurementResult(item) && variableUri === item.of_outcome;
    }

    function isResultForArm(armUri, item) {
      return isResult(item) && armUri === item.of_group;
    }

    function isResultForOutcome(outcomeUri, item) {
      return isResult(item) && outcomeUri === item.of_outcome;
    }

    function isStudyNode(node) {
      return node['@type'] === 'ontology:Study';
    }

    function isResult(node) {
      return node.of_outcome && node.of_group && node.of_moment;
    }

    function isNonConformantMeasurementResult(node) {
      return node.comment && node.of_outcome && node.of_group && !node.of_moment;
    }

    function isMoment(node) {
      return node['@type'] === 'ontology:MeasurementMoment';
    }

    function cleanupMeasurements() {
      return StudyService.getJsonGraph().then(function(graph) {
        // first get all the info we need
        var study;
        var hasArmMap;
        var hasGroupMap;
        var isMomentMap = {};
        var hasOutcomeMap;
        var isMeasurementOnOutcome = {};

        _.each(graph, function(node) {
          if (isStudyNode(node)) {
            study = node;
          }

          if (isMoment(node)) {
            isMomentMap[node['@id']] = true;
          }
        });

        hasArmMap = study.has_arm.reduce(function(accum, item) {
          accum[item['@id']] = true;
          return accum;
        }, {});

        hasGroupMap = study.has_group.reduce(function(accum, item) {
          accum[item['@id']] = true;
          return accum;
        }, {});

        if (study.has_included_population) {
          hasGroupMap[study.has_included_population[0]['@id']] = true;
        }

        hasOutcomeMap = study.has_outcome.reduce(function(accum, item) {
          accum[item['@id']] = true;
          return accum;
        }, {});

        // add al measurements that are selected on at least one outcome to the isMeasurementOnOutcome map
        isMeasurementOnOutcome = _.reduce(study.has_outcome, function(accum, outcome) {
          var measuredAtList;
          if (!Array.isArray(outcome.is_measured_at)) {
            measuredAtList = [outcome.is_measured_at];
          } else {
            measuredAtList = outcome.is_measured_at || [];
          }
          _.forEach(measuredAtList, function(measurementMomentUri) {
            isMeasurementOnOutcome[measurementMomentUri] = true;
          });
          return accum;
        }, isMeasurementOnOutcome);

        // now its time for cleaning
        var filteredGraph = _.filter(graph, function(node) {
          if (isResult(node)) {
            return (hasArmMap[node.of_group] || hasGroupMap[node.of_group]) &&
              isMomentMap[node.of_moment] &&
              hasOutcomeMap[node.of_outcome] &&
              isMeasurementOnOutcome[node.of_moment];
          } else {
            return true;
          }
        });

        return StudyService.saveJsonGraph(filteredGraph);
      });
    }

    function setToMeasurementMoment(measurementMomentUri, measurementInstanceList) {
      return StudyService.getJsonGraph().then(function(graph) {

        _.each(graph, function(node) {
          if (measurementInstanceList.indexOf(node['@id']) > -1) {
            node.of_moment = measurementMomentUri;
            delete node.comment;
          }
        });

        return StudyService.saveJsonGraph(graph);
      });
    }

    function isExistingMeasurement(measurementMomentUri, measurementInstanceList) {
      var nonConformantMeasurementInstance = measurementInstanceList[0];
      return StudyService.getJsonGraph().then(function(graph) {

        var nonConformantMeasurement = _.find(graph, function(node) {
          return node['@id'] === nonConformantMeasurementInstance;
        });

        return !!_.find(graph, function(node) {
          return isResult(node) &&
            node.of_moment === measurementMomentUri &&
            nonConformantMeasurement.of_group === node.of_group &&
            nonConformantMeasurement.of_outcome === node.of_outcome;
        });
      });
    }

    function _queryResults(uri, typeFunction) {
      return StudyService.getJsonGraph().then(function(graph) {
        var resultJsonItems = graph.filter(typeFunction.bind(this, uri));
        return resultJsonItems.reduce(toFrontend, []);
      });
    }

    function queryResults(variableUri) {
      return _queryResults(variableUri, isResultForVariable);
    }

    function queryResultsByGroup(armUri) {
      return _queryResults(armUri, isResultForArm);
    }

    function queryResultsByOutcome(outcomeUri) {
      return _queryResults(outcomeUri, isResultForOutcome);
    }

    function queryNonConformantMeasurements(outcomeUri) {
      return _queryResults(outcomeUri, isResultForNonConformantMeasurement);
    }

    return {
      updateResultValue: updateResultValue,
      queryResults: queryResults,
      queryResultsByGroup: queryResultsByGroup,
      queryResultsByOutcome: queryResultsByOutcome,
      queryNonConformantMeasurements: queryNonConformantMeasurements,
      cleanupMeasurements: cleanupMeasurements,
      setToMeasurementMoment: setToMeasurementMoment,
      isExistingMeasurement: isExistingMeasurement,
      isStudyNode: isStudyNode
    };
  };
  return dependencies.concat(ResultsService);
});
