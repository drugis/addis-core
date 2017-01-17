'use strict';
define(['lodash'], function(_) {
  var dependencies = ['StudyService'];
  var UnitService = function(StudyService) {

    function nodeToFrontEnd(node) {
      return {
        uri: node['@id'],
        label: node.label,
        conceptMapping: node.sameAs
      };
    }

    function updateActivities(study, unit, mergeTarget) {
      return _.map(study.has_activity, function(activity) {
        activity.has_drug_treatment = _.map(activity.has_drug_treatment, function(drugTreatment) {
          drugTreatment.treatment_dose = _.map(drugTreatment.treatment_dose, function(dose) {
            if (dose.unit === unit.uri) {
              dose.unit = mergeTarget.uri;
            }
            return dose;
          });
          return drugTreatment;
        });
        return activity;
      });
    }

    function merge(unit, mergeTarget) {
      return StudyService.getJsonGraph().then(function(graph) {
        var newGraph = _.chain(graph)
          .reject(['@id', unit.uri])
          .map(function(node) {
            if (node['@type'] === 'ontology:Study') {
              node.has_activity = updateActivities(node, unit, mergeTarget);
            }
            return node;
          }).value();
        return StudyService.saveJsonGraph(newGraph);
      });
    }

    function queryItems() {
      return StudyService.getJsonGraph().then(function(graph) {
        var nodes = _.filter(graph, function(node) {
          return node['@type'] === 'ontology:Unit';
        });
        return _.map(nodes, nodeToFrontEnd);
      });
    }

    return {
      queryItems: queryItems,
      merge: merge
    };
  };
  return dependencies.concat(UnitService);
});
