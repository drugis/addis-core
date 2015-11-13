'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService'];
    var UnitService = function($q, StudyService) {

      function queryItems(studyUuid) {
        return StudyService.getJsonGraph().then(function(graph) {
          return _.filter(graph['@graph'], function(node) {
            return node['@type'] === 'ontology:Unit';
          });
        });
      }

      return {
        queryItems: queryItems,
      };
    };
    return dependencies.concat(UnitService);
  });
