'use strict';
define([],
  function() {
    var dependencies = ['$q', 'OutcomeService'];
    var PopulationCharacteristicService = function($q, OutcomeService) {

      var populationCharacteristicType = 'ontology:PopulationCharacteristic';

      function queryItems() {
        return OutcomeService.queryItems(function(node) {
          return node['@type'] === populationCharacteristicType;
        });
      }

      function addItem(item) {
        return OutcomeService.addItem(item, populationCharacteristicType);
      }

      function deleteItem(item) {
        return OutcomeService.deleteItem(item);
      }

      function editItem(item) {
        return OutcomeService.editItem(item, populationCharacteristicType);
      }

      return {
        queryItems: queryItems,
        addItem: addItem,
        deleteItem: deleteItem,
        editItem: editItem
      };
    };
    return dependencies.concat(PopulationCharacteristicService);
  });
