'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService'];
    var UnitService = function($q, StudyService) {

        function nodeToFrontEnd(node) {
          return {
            uri: node['@id'],
            label: node.label
          };
        }

      function queryItems(studyUuid) {
        return StudyService.getJsonGraph().then(function(graph) {
          var nodes = _.filter(graph['@graph'], function(node) {
            return node['@type'] === 'ontology:Unit';
          });
          return _.map(nodes, nodeToFrontEnd);
        });
      }

      return {
        queryItems: queryItems,
      };
    };
    return dependencies.concat(UnitService);
  });
