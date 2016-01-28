'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService'];
    var DrugService = function($q, StudyService, SparqlResource, UUIDService) {

      var queryDrugTemplate = SparqlResource.get('queryDrug.sparql');

      function queryItems(studyUuid) {
        return queryDrugTemplate.then(function(template){
          // no need to fill template, nothing to fillin
          return StudyService.doNonModifyingQuery(template);
        });
      }

      return {
        queryItems: queryItems,
      };
    };
    return dependencies.concat(DrugService);
  });
