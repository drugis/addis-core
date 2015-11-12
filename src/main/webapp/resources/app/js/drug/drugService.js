'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService'];
    var DrugService = function($q, StudyService) {

      function queryItems() {
        return StudyService.getJsonGraph().then(function(graph) {
          return _.filter(graph['@graph'], function(node) {
            return node['@type'] === 'ontology:Drug';
          });
        });
      }

      function addItem(newDrug) {
        return StudyService.getJsonGraph().then(function(graph) {
          graph['@graph'].push(newDrug);
          StudyService.saveJsonGraph(graph);
        });
      }

      return {
        queryItems: queryItems,
        addItem: addItem
      };
    };
    return dependencies.concat(DrugService);
  });
