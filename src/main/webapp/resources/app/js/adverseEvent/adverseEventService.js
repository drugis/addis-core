'use strict';
define([],
  function() {
    var dependencies = ['$q', 'OutcomeService'];
    var AdverseEventService = function($q, OutcomeService) {

      var adverseEventType = 'ontology:AdverseEvent';

      function queryItems() {
        return OutcomeService.queryItems(function(node) {
          return node['@type'] === adverseEventType;
        });
      }

      function addItem(item) {
        return OutcomeService.addItem(item, adverseEventType)
      }

      function deleteItem(item) {
        return OutcomeService.deleteItem(item);
      }

      function editItem(item) {
        return OutcomeService.editItem(item, adverseEventType);
      }

      return {
        queryItems: queryItems,
        addItem: addItem,
        deleteItem: deleteItem,
        editItem: editItem
      };
    };
    return dependencies.concat(AdverseEventService);
  });
