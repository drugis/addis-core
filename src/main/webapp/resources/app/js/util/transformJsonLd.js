'use strict';
define(['lodash', './context'], function(_, externalContext) {
  function improveJsonLd(linkedData) {
    linkedData = removeSingleSubjectNotation(linkedData);
    linkedData = fixPropertyNames(linkedData);
    linkedData = usePrefixesInValues(linkedData);

    inlineObjectsForSubjectsWithProperty(linkedData, 'has_activity_application');
    inlineObjectsForSubjectsWithProperty(linkedData, 'of_variable');
    inlineObjectsForSubjectsWithProperty(linkedData, 'category_count');

    var titratedDrugTreatments = _.filter(linkedData['@graph'], propertyValuePredicate('@type', 'ontology:TitratedDoseDrugTreatment'));
    _.forEach(titratedDrugTreatments, function(tdt) {
      inlineObjects(linkedData, tdt, 'treatment_min_dose');
      inlineObjects(linkedData, tdt, 'treatment_max_dose');
    });

    var fixedDrugTreatments = _.filter(linkedData['@graph'], propertyValuePredicate('@type', 'ontology:FixedDoseDrugTreatment'));
    _.forEach(fixedDrugTreatments, function(fdt) {
      inlineObjects(linkedData, fdt, 'treatment_dose');
    });

    var treatmentActivities = _.filter(linkedData['@graph'], propertyValuePredicate('@type', 'ontology:TreatmentActivity'));
    _.forEach(treatmentActivities, function(activity) {
      inlineObjects(linkedData, activity, 'has_drug_treatment');
    });

    var study = _.find(linkedData['@graph'], propertyValuePredicate('@type', 'ontology:Study'));
    inlineObjects(linkedData, study, 'has_outcome');
    inlineObjects(linkedData, study, 'has_arm');
    inlineObjects(linkedData, study, 'has_group');
    inlineObjects(linkedData, study, 'has_included_population');
    inlineObjects(linkedData, study, 'has_activity');
    inlineObjects(linkedData, study, 'has_indication');
    inlineObjects(linkedData, study, 'has_objective');
    inlineObjects(linkedData, study, 'has_publication');
    inlineObjects(linkedData, study, 'has_eligibility_criteria');
    
    study.has_epochs = inlineLinkedList(linkedData, study.has_epochs);

    study.has_outcome = _.map(study.has_outcome, function(outcome) {
      if (typeof outcome.has_result_property === 'string') {
        outcome.has_result_property = [outcome.has_result_property];
      }
      return outcome;
    });

    study.has_outcome = inlineCategoryLists(linkedData, study.has_outcome, linkedData['@graph']);

    linkedData['@context'] = externalContext;

    return linkedData;
  }

  function propertyValuePredicate(property, value) {
    return function(subjectWithTriples) {
      return subjectWithTriples[property] === value;
    };
  }

  function removeSingleSubjectNotation(linkedData) {
    if (!linkedData['@graph']) {
      var context = linkedData['@context'];
      delete linkedData['@context'];
      linkedData = {
        '@graph': [linkedData],
        '@context': context
      };
    }
    return linkedData;
  }

  function fixPropertyNames(linkedData) {
    var contextPairs = _.toPairs(linkedData['@context']);
    var typedProperties = _.fromPairs(
      _.map(contextPairs, function(pair) {
        return [pair[1]['@id'], pair[0]];
      }));
    linkedData['@graph'] = _.map(linkedData['@graph'], function(subjectWithTriples) {
      return _.mapKeys(subjectWithTriples, function(v, k) {
        var mapped = typedProperties[k];
        return mapped ? mapped : k;
      });
    });
    return linkedData;
  }

  function usePrefixesInValues(linkedData) {
    var prefixes = {
      'ontology': 'http://trials.drugis.org/ontology#'
    };
    linkedData['@graph'] = _.map(linkedData['@graph'], function(subjectWithTriples) {
      return _.mapValues(subjectWithTriples, function(value) {
        if (typeof value === 'string') {
          return _.reduce(_.toPairs(prefixes), function(accum, pair) {
            return accum.replace(pair[1], pair[0] + ':');
          }, value);
        } else {
          return value;
        }
      });
    });
    _.assign(linkedData['@context'], prefixes);
    return linkedData;
  }

  function inlineCategoryLists(linkedData, outcomes) {
    return _.map(outcomes, function(node) {
      if (node.of_variable[0].measurementType === 'ontology:categorical') {
        node.of_variable[0].categoryList = inlineLinkedList(linkedData, node.of_variable[0].categoryList);
      }
      return node;
    });
  }

  function inlineLinkedList(linkedData, rootId) {
    var rdfListNil = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#nil';
    if (!rootId || rootId === rdfListNil) {
      return {
        '@id': rdfListNil
      };
    }
    var head = {};
    var tail = head;
    var listBlankNode = rootId;

    while (true) {
      if (listBlankNode['@list']) { // FIXME: make safe for > 1 item @lists (which we don't currently get)
        if (listBlankNode['@list'].length === 0) {
          return {
            '@id': rdfListNil
          };
        }
        if (listBlankNode['@list'][0]['@id']) {
          tail.first = findAndRemoveFromGraph(linkedData, listBlankNode['@list'][0]['@id']);
        } else {
          tail.first = findAndRemoveFromGraph(linkedData, listBlankNode['@list'][0]);
        }
        tail.rest = {
          '@id': rdfListNil
        };
        return head;
      } else if (listBlankNode['@id'] === rdfListNil || listBlankNode === rdfListNil) {
        tail['@id'] = rdfListNil;
        return head;
      } else {
        listBlankNode = listBlankNode['@id'] ? findAndRemoveFromGraph(linkedData, listBlankNode['@id']) : findAndRemoveFromGraph(linkedData, listBlankNode);
        tail['@id'] = listBlankNode['@id'];
        if (listBlankNode.first && listBlankNode.first['@id']) {
          tail.first = findAndRemoveFromGraph(linkedData, listBlankNode.first['@id']);
        } else if (_.isString(listBlankNode.first)) {
          tail.first = findAndRemoveFromGraph(linkedData, listBlankNode.first);
        } else {
          tail.first = listBlankNode.first;
        }
        listBlankNode = listBlankNode.rest;
        tail.rest = {};
        tail = tail.rest;
      }
    }
  }

  function findAndRemoveFromGraph(linkedData, id) {
    var result = _.find(linkedData['@graph'], propertyValuePredicate('@id', id));
    linkedData['@graph'] = _.without(linkedData['@graph'], result);
    return result;
  }

  function inlineObjects(linkedData, subject, propertyName) {
    var arr = subject[propertyName];
    if (typeof arr === 'string') {
      arr = [arr];
    }
    subject[propertyName] = _.map(arr, function(id) {
      return findAndRemoveFromGraph(linkedData, id);
    });
  }

  function inlineObjectsForSubjectsWithProperty(linkedData, propertyName) {
    var subjectList = linkedData['@graph'];
    var subjectsWithProperty = _.filter(subjectList, function(subjectWithTriples) {
      return subjectWithTriples[propertyName];
    });
    _.forEach(subjectsWithProperty, function(subject) {
      inlineObjects(linkedData, subject, propertyName);
    });
  }

  return improveJsonLd;
});
