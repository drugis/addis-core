'use strict';
define([],
  function() {
    var dependencies = ['$q', 'OutcomeService'];
    var EndpointService = function($q, OutcomeService) {

      var endpointType = 'ontology:Endpoint';

      function queryItems() {
        return OutcomeService.queryItems(function(node) {
          return node['@type'] === endpointType;
        });
      }

      function addItem(item) {
        return OutcomeService.addItem(item, endpointType);
      }

      function deleteItem(item) {
        return OutcomeService.deleteItem(item);
      }

      function editItem(item) {
        return OutcomeService.editItem(item, endpointType);
      }
      return {
        queryItems: queryItems,
        addItem: addItem,
        deleteItem: deleteItem,
        editItem: editItem
      };
    };
    return dependencies.concat(EndpointService);
  });
