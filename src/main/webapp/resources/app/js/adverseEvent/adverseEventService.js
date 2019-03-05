'use strict';
define([],
  function() {
    var dependencies = ['OutcomeService'];
    var AdverseEventService = function(OutcomeService) {

      var TYPE = 'ontology:AdverseEvent';

      function queryItems() {
        return OutcomeService.queryItems(function(node) {
          return node['@type'] === TYPE;
        });
      }

      function addItem(item) {
        return OutcomeService.addItem(item, TYPE);
      }

      function deleteItem(item) {
        return OutcomeService.deleteItem(item);
      }

      function editItem(item) {
        return OutcomeService.editItem(item, TYPE);
      }

      return {
        queryItems: queryItems,
        addItem: addItem,
        deleteItem: deleteItem,
        editItem: editItem,
        TYPE: TYPE
      };
    };
    return dependencies.concat(AdverseEventService);
  });
