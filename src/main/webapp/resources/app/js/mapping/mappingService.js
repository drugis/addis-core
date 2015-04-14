'use strict';
define([],
  function() {
    var dependencies = ['StudyService', 'SparqlResource'];
    var MappingService = function(StudyService, SparqlResource) {

      var setDrugMappingTemplate = SparqlResource.get('setDrugMapping.sparql');
      var removeMappingTemplate = SparqlResource.get('removeDrugMapping.sparql');

      function updateMapping(studyConcept, datasetConcept) {
        return setDrugMappingTemplate.then(function(template) {
          var query = fillInTemplate(template, studyConcept.uri, datasetConcept.uri);
          return StudyService.doModifyingQuery(query);
        });
      }

      function removeMapping(studyConcept, datasetConcept) {
        return removeMappingTemplate.then(function(template) {
          var query = fillInTemplate(template, studyConcept.uri, datasetConcept.uri);
          return StudyService.doModifyingQuery(query);
        });
      }

      function fillInTemplate(template, studyConceptUri, datasetConceptUri) {
        return template
          .replace(/\$studyConcept/g, studyConceptUri)
          .replace(/\$newDatasetConcept/g, datasetConceptUri);
      }

      return {
        updateMapping: updateMapping,
        removeMapping: removeMapping
      };


    };

    return dependencies.concat(MappingService);
  });