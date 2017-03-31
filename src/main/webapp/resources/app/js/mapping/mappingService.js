'use strict';
define(['lodash'], function(_) {
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
      } else if (datasetConcept['@type'] === 'ontology:Unit') {
        return updateUnitMapping(studyConcept, datasetConcept);
      } else {
        throw 'Attempt to map unknown type of concept';
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

    function updateUnitMapping(studyConcept, datasetConcept) {
      return StudyService.getJsonGraph().then(function(graph) {
        var unitNode = findNodeWithId(graph, studyConcept.uri);
        unitNode.sameAs = datasetConcept['@id'];
        unitNode.conversionMultiplier = studyConcept.conversionMultiplier;
        return StudyService.saveJsonGraph(graph);
      });
    }

    function removeMapping(studyConcept, datasetConcept) {
      if (datasetConcept['@type'] === 'ontology:Unit') {
        return removeUnitMapping(studyConcept);
      } else if (datasetConcept['@type'] === 'ontology:Variable') {
        return removeVariableMapping(studyConcept);
      } else if (datasetConcept['@type'] === 'ontology:Drug') {
        return removeDrugMapping(studyConcept);
      }
    }

    function removeDrugMapping(studyConcept) {
      return StudyService.getJsonGraph().then(function(graph) {
        var node = findNodeWithId(graph, studyConcept.uri);
        delete node.sameAs;
        return StudyService.saveJsonGraph(graph);
      });

    }

    function removeUnitMapping(studyConcept) {
      return StudyService.getJsonGraph().then(function(graph) {
        var node = findNodeWithId(graph, studyConcept.uri);
        delete node.sameAs;
        delete node.conversionMultiplier;
        return StudyService.saveJsonGraph(graph);
      });
    }

    function removeVariableMapping(studyConcept) {
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
