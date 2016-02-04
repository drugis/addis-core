'use strict';
define(['angular','lodash'], function(angular, _) {
    var dependencies = ['StudyService', 'UUIDService'];
    var ResultsService = function(StudyService, UUIDService) {

      function updateResultValue(row, inputColumn) {
        return StudyService.getJsonGraph().then(function(graph) {
          if (!row.uri) {
            if (inputColumn.value) {
              // create branch
              var addItem = {
                '@id': 'http://trials.drugis.org/instances/' + UUIDService.generate(),
                of_arm: row.arm.armURI,
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

      function toFrontend(acum, backEndItem) {
        // ?instance ?armUri ?momentUri ?result_property ?value
        var baseItem = {
          instance: backEndItem['@id'],
          armUri: backEndItem.of_arm,
          momentUri: backEndItem.of_moment,
          outcomeUri: backEndItem.of_outcome,
        };

        if (backEndItem.sample_size) {
          acum.push(createValueItem(baseItem, backEndItem, 'sample_size'));
        }

        if (backEndItem.count) {
          acum.push(createValueItem(baseItem, backEndItem, 'count'));
        }

        if (backEndItem.standard_deviation) {
          acum.push(createValueItem(baseItem, backEndItem, 'standard_deviation'));
        }

        if (backEndItem.mean) {
          acum.push(createValueItem(baseItem, backEndItem, 'mean'));
        }

        return acum;
      }

      function isResultForVariable(variableUri, item) {
        return isResult(item) && variableUri === item.of_outcome;
      }

      function isStudyNode(node) {
        return node['@type'] === 'ontology:Study';
      }

      function isResult(node) {
        return node.of_outcome && node.of_arm && node.of_moment;
      }

      function isMoment(node) {
        return node['@type'] === 'ontology:MeasurementMoment';
      }

      function cleanupMeasurements() {
        return StudyService.getJsonGraph().then(function(graph) {
          // first get all the info we need
          var study;
          var hasArmMap = {};
          var momentMap = {};
          var hasOutcomeMap = {};

          _.each(graph, function(node) {

            if (isStudyNode(node)) {
              study = node;
            }

            if (isMoment(node)) {
              momentMap[node['@id']] = true;
            }

          });

          study.has_arm.reduce(function(accum, item) {
            accum[item['@id']] = true;
            return accum;
          }, hasArmMap);

          study.has_outcome.reduce(function(accum, item) {
            accum[item['@id']] = true;
            return accum;
          }, hasOutcomeMap);

          // now its time for cleaning
          var filterdGraph = _.filter(graph, function(node) {
            if (isResult(node)) {
              return hasArmMap[node.of_arm] && momentMap[node.of_moment] && hasOutcomeMap[node.of_outcome];
            } else {
              return true;
            }
          });

          return StudyService.saveJsonGraph(filterdGraph);
        });
      }

      function queryResults(variableUri) {
        return StudyService.getJsonGraph().then(function(graph) {
          var resutJsonItems = graph.filter(isResultForVariable.bind(this, variableUri));
          return resutJsonItems.reduce(toFrontend, []);
        });
      }

      return {
        updateResultValue: updateResultValue,
        queryResults: queryResults,
        cleanupMeasurements: cleanupMeasurements
      };
    };
    return dependencies.concat(ResultsService);
  });
