'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource'];
    var PopulationInformationService = function($q, StudyService, SparqlResource) {

      var populationInformationQuery = SparqlResource.get('queryPopulationInformation.sparql');
    //  var editPopulationInformationTemplate = SparqlResource.get('editPopulationInformation.sparql');

      function queryItems() {
        return populationInformationQuery.then(function(query) {
          return StudyService.doNonModifyingQuery(query).then(function(result){
            if(result.length === 0) {
              result.push({});
            }
            return result;
          });
        });
      }

      return {
        queryItems: queryItems
      };
    };
    return dependencies.concat(PopulationInformationService);
  });