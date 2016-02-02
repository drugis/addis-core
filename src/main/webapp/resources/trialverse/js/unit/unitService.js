'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService'];
    var UnitService = function($q, StudyService, SparqlResource, UUIDService) {

      var queryUnitTemplate = SparqlResource.get('queryUnit.sparql');

      function queryItems(studyUuid) {
        return queryUnitTemplate.then(function(template){
          // no need to fill template, nothing to fillin
          return StudyService.doNonModifyingQuery(template);
        });
      }

      return {
        queryItems: queryItems,
      };
    };
    return dependencies.concat(UnitService);
  });
