'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'SparqlResource'];
    var PopulationInformationService = function($q, StudyService, UUIDService, SparqlResource) {

      var addPopulationInformationQueryRaw = SparqlResource.get('addPopulationInformation.sparql');
      var PopulationInformationsQuery = SparqlResource.get('queryPopulationInformation.sparql');

      function queryItems() {

      }

      function editItem(item) {

      }

      return {
        queryItems: queryItems,
        editItem: editItem
      };
    };
    return dependencies.concat(PopulationInformationService);
  });
