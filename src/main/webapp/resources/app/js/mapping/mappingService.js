'use strict';
define([],
  function() {
    var dependencies = ['StudyService', 'SparqlResource'];
    var MappingService = function(StudyService, SparqlResource) {

      function findDrugNode(graph, studyConceptUri) {
        return _.find(graph, function(node) {
          return node['@id'] === studyConceptUri;
        });
      }

      function updateDrugMapping(studyConcept, datasetConcept) {
        return StudyService.getJsonGraph().then(function(graph) {
          var drugNode = findDrugNode(graph, studyConcept.uri);
          drugNode.sameAs = datasetConcept.uri;
          return StudyService.saveGraph(graph);
        });
      }

      function updateVariableMapping(studyConcept, datasetConcept) {
        return StudyService.getStudy().then(function(study) {

        });
      }

      function updateMapping(studyConcept, datasetConcept) {
        if (datasetConcept.type === 'ontology:Drug') {
          return updateDrugMapping(studyConcept, datasetConcept);
        } else if (datasetConcept.type === 'ontology:PopulationCharacteristic' ||
          datasetConcept.type === 'ontology:AdverseEvent' ||
          datasetConcept.type === 'ontology:Endpoint') {
          return updateVariableMapping(studyConcept, datasetConcept);
        }
      }

      function removeMapping(studyConcept, datasetConcept) {
        return StudyService.getJsonGraph().then(function(graph) {
          var drugNode = findDrugNode(graph, studyConcept.uri);
          delete drugNode.sameAs;
          return StudyService.saveGraph(graph);
        });
      }

      return {
        updateMapping: updateMapping,
        removeMapping: removeMapping
      };


    };

    return dependencies.concat(MappingService);
  });
