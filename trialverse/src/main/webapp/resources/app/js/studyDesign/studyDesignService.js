'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService'];
    var StudyDesignService = function($q, StudyService, SparqlResource, UUIDService) {

      var queryActivityCoordinatesTemplate = SparqlResource.get('queryActivityCoordinates.sparql');
      var setActivityCoordinatesTemplate = SparqlResource.get('setActivityCoordinates.sparql');
      var cleanupCoordinatesTemplate = SparqlResource.get('cleanupCoordinates.sparql');

      function queryItems() {
        return queryActivityCoordinatesTemplate.then(function(template){
          var query = fillInTemplate(template, {});
          return StudyService.doNonModifyingQuery(query);
        });
      }

      function setActivityCoordinates(coordinates) {
        return setActivityCoordinatesTemplate.then(function(template){
          var query = fillInTemplate(template, coordinates);
          return StudyService.doModifyingQuery(query);
        });
      }

      function cleanupCoordinates() {
        return cleanupCoordinatesTemplate.then(function(template) {
          var query = fillInTemplate(template, {});
          return StudyService.doModifyingQuery(query);
        });
      }

      function fillInTemplate(template, coordinates) {
        return template
            .replace(/\$epochUri/g, coordinates.epochUri)
            .replace(/\$armUri/g, coordinates.armUri)
            .replace(/\$activityUri/g, coordinates.activityUri);
      }

      return {
        queryItems: queryItems,
        setActivityCoordinates: setActivityCoordinates,
        cleanupCoordinates: cleanupCoordinates
      };
    };
    return dependencies.concat(StudyDesignService);
  });
