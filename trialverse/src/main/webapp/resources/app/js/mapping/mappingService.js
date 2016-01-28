'use strict';
define([],
  function() {
    var dependencies = ['StudyService', 'SparqlResource'];
    var MappingService = function(StudyService, SparqlResource) {

      var setDrugMappingTemplate = SparqlResource.get('setDrugMapping.sparql');
      var setVariableMappingTemplate = SparqlResource.get('setVariableMapping.sparql');
      var removeMappingTemplate = SparqlResource.get('removeDrugMapping.sparql');

      function updateDrugMapping(studyConcept, datasetConcept) {
        return setDrugMappingTemplate.then(function(template) {
          var query = fillInTemplate(template, studyConcept.uri, datasetConcept.uri);
          return StudyService.doModifyingQuery(query);
        });
      }

      function updateVariableMapping(studyConcept, datasetConcept) {
        return setVariableMappingTemplate.then(function(template) {
          var query = fillInTemplate(template, studyConcept.uri, datasetConcept.uri);
          return StudyService.doModifyingQuery(query);
        });
      }

      function updateMapping(studyConcept, datasetConcept) {
        if (datasetConcept.type === 'http://trials.drugis.org/ontology#Drug') {
          return updateDrugMapping(studyConcept, datasetConcept);
        } else {
          return updateVariableMapping(studyConcept, datasetConcept);
        }
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
          .replace(/\$datasetConcept/g, datasetConceptUri);
      }

      return {
        updateMapping: updateMapping,
        removeMapping: removeMapping
      };


    };

    return dependencies.concat(MappingService);
  });
