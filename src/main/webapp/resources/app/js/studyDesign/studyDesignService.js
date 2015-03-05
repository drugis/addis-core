'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService'];
    var StudyDesignService = function($q, StudyService, SparqlResource, UUIDService) {

      var queryActivityCoordinatesTemplate = SparqlResource.get('queryActivityCoordinates.sparql');
      var setActivityCoordinatesTemplate = SparqlResource.get('setActivityCoordinates.sparql');

      function queryItems(studyUuid) {
        return queryActivityCoordinatesTemplate.then(function(template){
          var query = fillInTemplate(template, studyUuid, {}); 
          return StudyService.doNonModifyingQuery(query);
        });
      }

      function setActivityCoordinates(studyUuid, coordinates) {
        return setActivityCoordinatesTemplate.then(function(template){
          var query = fillInTemplate(template, studyUuid, coordinates);
          return StudyService.doModifyingQuery(query);
        });
      }

      function fillInTemplate(template, studyUuid, coordinates) {
        return template
            .replace(/\$studyUuid/g, studyUuid)
            .replace(/\$epochUri/g, coordinates.epochUri)
            .replace(/\$armUri/g, coordinates.armUri)
            .replace(/\$activityUri/g, coordinates.activityUri);
      }

      return {
        queryItems: queryItems,
        setActivityCoordinates: setActivityCoordinates
      };
    };
    return dependencies.concat(StudyDesignService);
  });
