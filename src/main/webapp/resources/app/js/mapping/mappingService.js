'use strict';
define([],
  function() {
    var dependencies = ['StudyService'];
    var MappingService = function(StudyService) {

      function findNodeWithId(graph, uri) {
        return _.find(graph, function(node) {
          return node['@id'] === uri;
        });
      }

      function updateMapping(studyConcept, datasetConcept) {
        if (datasetConcept['@type'] === 'ontology:Drug') {
          return updateDrugMapping(studyConcept, datasetConcept);
        } else if (datasetConcept['@type'] === 'ontology:Variable') {
          return updateVariableMapping(studyConcept, datasetConcept);
        }
      }

      function updateDrugMapping(studyConcept, datasetConcept) {
        return StudyService.getJsonGraph().then(function(graph) {
          var drugNode = findNodeWithId(graph, studyConcept.uri);
          drugNode.sameAs = datasetConcept['@id'];
          return StudyService.saveJsonGraph(graph);
        });
      }

      function updateVariableMapping(studyConcept, datasetConcept) {
        return StudyService.getStudy().then(function(study) {
          var outcomeNode = findNodeWithId(study.has_outcome, studyConcept.uri);
          outcomeNode.of_variable[0].sameAs = datasetConcept['@id'];
          return StudyService.save(study);
        });
      }

      function removeMapping(studyConcept, datasetConcept) {
        if (datasetConcept['@type'] === 'ontology:Drug') {
          return removeDrugMapping(studyConcept, datasetConcept);
        } else if (datasetConcept['@type'] === 'ontology:Variable') {
          return removeVariableMapping(studyConcept, datasetConcept);
        }
      };

      function removeDrugMapping(studyConcept, datasetConcept) {
        return StudyService.getJsonGraph().then(function(graph) {
          var drugNode = findNodeWithId(graph, studyConcept.uri);
          delete drugNode.sameAs;
          return StudyService.saveJsonGraph(graph);
        });
      }

      function removeVariableMapping(studyConcept, datasetConcept) {
        return StudyService.getStudy().then(function(study) {
          var outcomeNode = findNodeWithId(study.has_outcome, studyConcept.uri);
          delete outcomeNode.of_variable[0].sameAs;
          return StudyService.save(study);
        });
      }

      return {
        updateMapping: updateMapping,
        removeMapping: removeMapping
      };


    };

    return dependencies.concat(MappingService);
  });
