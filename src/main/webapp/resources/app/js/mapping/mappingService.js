'use strict';
define(['lodash'], function(_) {
  var dependencies = [
    '$q',
    'StudyService'
  ];
  var MappingService = function(
    $q,
    StudyService
  ) {

    var METRIC_MULTIPLIERS = [{
      label: 'nano',
      conversionMultiplier: 1e-09
    }, {
      label: 'micro',
      conversionMultiplier: 1e-06
    }, {
      label: 'milli',
      conversionMultiplier: 1e-03
    }, {
      label: 'centi',
      conversionMultiplier: 1e-02
    }, {
      label: 'deci',
      conversionMultiplier: 1e-01
    }, {
      label: 'no multiplier',
      conversionMultiplier: 1e00
    }, {
      label: 'deca',
      conversionMultiplier: 1e01
    }, {
      label: 'hecto',
      conversionMultiplier: 1e02
    }, {
      label: 'kilo',
      conversionMultiplier: 1e03
    }, {
      label: 'mega',
      conversionMultiplier: 1e06
    }];

    function findNodeWithId(graph, uri) {
      return _.find(graph, function(node) {
        return node['@id'] === uri;
      });
    }

    function updateMapping(studyConcept, datasetConcept) {
      switch (datasetConcept['@type']) {
        case 'ontology:Drug':
          return updateDrugMapping(studyConcept, datasetConcept);
        case 'ontology:Variable':
          return updateVariableMapping(studyConcept, datasetConcept);
        case 'ontology:Unit':
          return updateUnitMapping(studyConcept, datasetConcept);
        default:
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

    function getUnits(constraint) {
      if (!constraint) {
        return [];
      }
      var desiredProperties = ['unitName', 'unitConcept', 'conversionMultiplier'];
      var units = [_.pick(constraint.lowerBound, desiredProperties), _.pick(constraint.upperBound, desiredProperties)];
      return _.reject(units, _.isEmpty);
    }

    function getUnitsFromIntervention(intervention) {
      var units;
      if (intervention.type === 'fixed') {
        units = getUnits(intervention.constraint);
      } else if (intervention.type === 'titrated' || intervention.type === 'both') {
        units = getUnits(intervention.minConstraint).concat(getUnits(intervention.maxConstraint));
      }
      return _.uniqWith(units, function(unit1, unit2) {
        return _.isEqual(
          _.pick(unit1, ['unitName', 'unitConcept']),
          _.pick(unit2, ['unitName', 'unitConcept']));
      });
    }

    function hasDoubleMapping(studyConcept, datasetConcept) {
      switch (datasetConcept['@type']) {
        case 'ontology:Drug':
          return findDoubleMappingForDrug(studyConcept, datasetConcept);
        case 'ontology:Variable':
          return findDoubleMappingForVariable(studyConcept, datasetConcept);
        default:
          return $q.resolve(false);
      }
    }

    function findDoubleMappingForVariable(studyConcept, datasetConcept) {
      return StudyService.getJsonGraph().then(function(graph) {
        var study = StudyService.findStudyNode(graph);
        var outcomes = study.has_outcome;
        return _.find(outcomes, function(outcome2) {
          return studyConcept.uri !== outcome2['@id'] &&
            datasetConcept['@id'] === outcome2.of_variable[0].sameAs;
        });
      });
    }

    function findDoubleMappingForDrug(studyConcept, datasetConcept) {
      return StudyService.getJsonGraph().then(function(graph) {
        return _.find(graph, function(node) {
          return node['@type'] === 'ontology:Drug' &&
            studyConcept.uri !== node['@id'] &&
            datasetConcept['@id'] === node.sameAs;
        });
      });
    }

    return {
      updateMapping: updateMapping,
      removeMapping: removeMapping,
      getUnitsFromIntervention: getUnitsFromIntervention,
      METRIC_MULTIPLIERS: METRIC_MULTIPLIERS,
      hasDoubleMapping: hasDoubleMapping
    };

  };

  return dependencies.concat(MappingService);
});
