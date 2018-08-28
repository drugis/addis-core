'use strict';
define(['lodash', './context'], function(_, externalContext) {
  function propertyValuePredicate(property, value) {
    return function(subjectWithTriples) {
      return subjectWithTriples[property] === value;
    };
  }

  function improveJsonLd(linkedData) {
    // annoying single-subject notation
    if (!linkedData['@graph']) {
      var context = linkedData['@context'];
      delete linkedData['@context'];
      linkedData = {
        '@graph': [linkedData],
        '@context': context
      };
    }

    // fix property names (also forces data types)
    var contextPairs = _.toPairs(linkedData['@context']);
    var typedProperties = _.fromPairs(
      _.map(contextPairs, function(pair) {
        return [pair[1]['@id'], pair[0]];
      }));
    linkedData['@graph'] = _.map(linkedData['@graph'], function(subjectWithTriples) {
      /* jslint unused: true  */
      return _.mapKeys(subjectWithTriples, function(v, k) {
        var mapped = typedProperties[k];
        return mapped ? mapped : k;
      });
    });

    // use prefixes in values
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

    function findAndRemoveFromGraph(id) {
      var result = _.find(linkedData['@graph'], propertyValuePredicate('@id', id));
      linkedData['@graph'] = _.without(linkedData['@graph'], result);
      return result;
    }

    function inlineObjects(subject, propertyName) {
      var arr = subject[propertyName];
      if (typeof arr === 'string') {
        arr = [arr];
      }
      subject[propertyName] = _.map(arr, findAndRemoveFromGraph);
    }

    function inlineObjectsForSubjectsWithProperty(subjectList, propertyName) {
      var subjectsWithProperty = _.filter(subjectList, function(subjectWithTriples) {
        return subjectWithTriples[propertyName];
      });
      _.forEach(subjectsWithProperty, function(subject) {
        inlineObjects(subject, propertyName);
      });
    }

    function inlineLinkedList(rootId) {
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
            tail.first = findAndRemoveFromGraph(listBlankNode['@list'][0]['@id']);
          } else {
            tail.first = findAndRemoveFromGraph(listBlankNode['@list'][0]);
          }
          tail.rest = {
            '@id': rdfListNil
          };
          return head;
        } else if (listBlankNode['@id'] === rdfListNil || listBlankNode === rdfListNil) {
          tail['@id'] = rdfListNil;
          return head;
        } else {
          listBlankNode = listBlankNode['@id'] ? findAndRemoveFromGraph(listBlankNode['@id']) : findAndRemoveFromGraph(listBlankNode);
          tail['@id'] = listBlankNode['@id'];
          if (listBlankNode.first && listBlankNode.first['@id']) {
            tail.first = findAndRemoveFromGraph(listBlankNode.first['@id']);
          } else if (_.isString(listBlankNode.first)) {
            tail.first = findAndRemoveFromGraph(listBlankNode.first);
          } else {
            tail.first = listBlankNode.first;
          }
          listBlankNode = listBlankNode.rest;
          tail.rest = {};
          tail = tail.rest;
        }
      }
    }

    function inlineCategoryLists(outcomes) {
      return _.map(outcomes, function(node) {
        if (node.of_variable[0].measurementType === 'ontology:categorical') {
          node.of_variable[0].categoryList = inlineLinkedList(node.of_variable[0].categoryList);
        }
        return node;
      });
    }

    inlineObjectsForSubjectsWithProperty(linkedData['@graph'], 'has_activity_application');
    inlineObjectsForSubjectsWithProperty(linkedData['@graph'], 'of_variable');
    inlineObjectsForSubjectsWithProperty(linkedData['@graph'], 'category_count');

    var titratedDrugTreatments = _.filter(linkedData['@graph'], propertyValuePredicate('@type', 'ontology:TitratedDoseDrugTreatment'));
    _.forEach(titratedDrugTreatments, function(tdt) {
      inlineObjects(tdt, 'treatment_min_dose');
      inlineObjects(tdt, 'treatment_max_dose');
    });

    var fixedDrugTreatments = _.filter(linkedData['@graph'], propertyValuePredicate('@type', 'ontology:FixedDoseDrugTreatment'));
    _.forEach(fixedDrugTreatments, function(fdt) {
      inlineObjects(fdt, 'treatment_dose');
    });

    var treatmentActivities = _.filter(linkedData['@graph'], propertyValuePredicate('@type', 'ontology:TreatmentActivity'));
    _.forEach(treatmentActivities, function(activity) {
      inlineObjects(activity, 'has_drug_treatment');
    });

    var study = _.find(linkedData['@graph'], propertyValuePredicate('@type', 'ontology:Study'));
    inlineObjects(study, 'has_outcome');
    inlineObjects(study, 'has_arm');
    inlineObjects(study, 'has_group');
    inlineObjects(study, 'has_included_population');
    inlineObjects(study, 'has_activity');
    inlineObjects(study, 'has_indication');
    inlineObjects(study, 'has_objective');
    inlineObjects(study, 'has_publication');
    inlineObjects(study, 'has_eligibility_criteria');
    study.has_epochs = inlineLinkedList(study.has_epochs);

    study.has_outcome = _.map(study.has_outcome, function(outcome) {
      if (typeof outcome.has_result_property === 'string') {
        outcome.has_result_property = [outcome.has_result_property];
      }
      return outcome;
    });

    study.has_outcome = inlineCategoryLists(study.has_outcome, linkedData['@graph']);

    linkedData['@context'] = externalContext;

    return linkedData;
  }

  return improveJsonLd;
});
