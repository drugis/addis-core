'use strict';
define([],
  function() {
    var dependencies = ['StudyService', 'SparqlResource'];
    var MappingService = function(StudyService, SparqlResource) {

      function findNodeWithId(graph, uri) {
        return _.find(graph, function(node) {
          return node['@id'] === uri;
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

      function updateDrugMapping(studyConcept, datasetConcept) {
        return StudyService.getJsonGraph().then(function(graph) {
          var drugNode = findNodeWithId(graph, studyConcept.uri);
          drugNode.sameAs = datasetConcept.uri;
          return StudyService.saveGraph(graph);
        });
      }

      function updateVariableMapping(studyConcept, datasetConcept) {
        return StudyService.getStudy().then(function(study) {
          var outcomeNode = findNodeWithId(study.has_outcome, studyConcept.uri);
          outcomeNode.of_variable[0].sameAs = datasetConcept.uri;
          return StudyService.saveStudy(study);
        });
      }

      function removeMapping(studyConcept, datasetConcept) {
        if (datasetConcept.type === 'ontology:Drug') {
          return removeDrugMapping(studyConcept, datasetConcept);
        } else if (datasetConcept.type === 'ontology:PopulationCharacteristic' ||
          datasetConcept.type === 'ontology:AdverseEvent' ||
          datasetConcept.type === 'ontology:Endpoint') {
          return removeVariableMapping(studyConcept, datasetConcept);
        }
      };

      function removeDrugMapping(studyConcept, datasetConcept) {
        return StudyService.getJsonGraph().then(function(graph) {
          var drugNode = findNodeWithId(graph, studyConcept.uri);
          delete drugNode.sameAs;
          return StudyService.saveGraph(graph);
        });
      }

      function removeVariableMapping(studyConcept, datasetConcept) {
        return StudyService.getStudy().then(function(study) {
          var outcomeNode = findNodeWithId(study.has_outcome, studyConcept.uri);
          delete outcomeNode.of_variable[0].sameAs;
          return StudyService.saveStudy(study);
        });
      }

      return {
        updateMapping: updateMapping,
        removeMapping: removeMapping
      };


    };

    return dependencies.concat(MappingService);
  });
