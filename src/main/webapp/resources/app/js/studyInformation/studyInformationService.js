'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'SparqlResource', 'UUIDService'];
    var StudyInformationService = function($q, StudyService, SparqlResource, UUIDService) {

      var studyInformationQuery = SparqlResource.get('queryStudyInformation.sparql');
      var editStudyInformationTemplate = SparqlResource.get('editStudyInformation.sparql');

      function queryItems() {
        return studyInformationQuery.then(function(query) {
          return StudyService.doNonModifyingQuery(query).then(function(result) {
            var transformedResult = {
              blinding: {},
            };
            if (result.length > 0) {
              transformedResult.blinding.uri = result[0].blindingUri;
            }
            return [transformedResult];
          });
        });
      }

      function editItem(item) {
        return editStudyInformationTemplate.then(function(template) {
          var query = fillTemplate(template, item);
          return StudyService.doModifyingQuery(query);
        });
      }

      function fillTemplate(template, item) {
                return template
          .replace(/\$blindingUri/g, item.blinding.uri)
          ;
      }

      return {
        queryItems: queryItems,
        editItem: editItem
      }
    };

    return dependencies.concat(StudyInformationService);
  });