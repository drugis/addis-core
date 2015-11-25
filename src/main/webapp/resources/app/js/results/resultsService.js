'use strict';
define([],
  function() {
    var dependencies = ['StudyService', 'UUIDService'];
    var ResultsService = function(StudyService, UUIDService) {

      function updateResultValue(row, inputColumn) {
        return StudyService.getJsonGraph().then(function(graph) {
          if (!row.uri) {
            // create branch
            var addItem = {
              '@id': 'http://trials.drugis.org/instances/' + UUIDService.generate(),
              of_arm: row.arm.uri,
              of_moment: row.measurementMoment.uri,
              of_outcome: row.variable.uri
            };
            addItem[inputColumn.valueName] = inputColumn.value;
            return StudyService.saveJsonGraph(graph.concat(addItem));
          } else {
            // update branch
            var editItem = _.remove(graph, function(node) {
              return row.uri = node['@id'];
            })[0];

            if (inputColumn.value === null) {
              delete editItem[inputColumn.valueName];
            } else {
              editItem[inputColumn.valueName] = inputColumn.value;
            }

            if (!isEmptyResult(editItem)) {
              graph.push(editItem);
            }

            return StudyService.saveJsonGraph(graph);
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
        }

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
          acum.push(createValueItem(backEndItem, 'mean'));
        }

        return acum;
      }

      function isResultForVariable(variableUri, item) {
        return item.of_outcome && variableUri === item.of_outcome && item.of_arm && item.of_moment;
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
      };
    };
    return dependencies.concat(ResultsService);
  });