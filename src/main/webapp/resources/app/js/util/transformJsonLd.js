'use strict';
define(['lodash'], function(_) {
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

    function inlineLinkedList(study, propertyName) {
      var rdfListNil = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#nil';
      if (!study[propertyName]) {
        return {
          '@id': rdfListNil
        };
      }

      var head = {};
      var tail = head;

      var listBlankNode = study[propertyName];

      while (true) {
        if (listBlankNode['@list']) { // FIXME: make safe for > 1 item @lists (which we don't currently get)
          if (listBlankNode['@list'].length === 0) {
            return head;
          }
          tail.first = findAndRemoveFromGraph(listBlankNode['@list'][0]);
          tail.rest = {
            '@id': rdfListNil
          };
          return head;
        } else if (listBlankNode['@id'] === rdfListNil || listBlankNode === rdfListNil) {
          tail['@id'] = rdfListNil;
          return head;
        } else {
          listBlankNode = findAndRemoveFromGraph(listBlankNode);
          tail['@id'] = listBlankNode['@id'];
          if (listBlankNode.first['@id']) {
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
    // study.has_epochs = inlineLinkedList(study, 'has_epochs');

    linkedData['@context'] = {
      'median': {
        '@id': 'http://trials.drugis.org/ontology#median',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'geometric_mean': {
        '@id': 'http://trials.drugis.org/ontology#geometric_mean',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'log_mean': {
        '@id': 'http://trials.drugis.org/ontology#log_mean',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'least_squares_mean': {
        '@id': 'http://trials.drugis.org/ontology#least_squares_mean',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'quantile_0.05': {
        '@id': 'http://trials.drugis.org/ontology#quantile_0.05',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'quantile_0.95': {
        '@id': 'http://trials.drugis.org/ontology#quantile_0.95',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'quantile_0.025': {
        '@id': 'http://trials.drugis.org/ontology#quantile_0.025',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'quantile_0.975': {
        '@id': 'http://trials.drugis.org/ontology#quantile_0.975',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'min': {
        '@id': 'http://trials.drugis.org/ontology#min',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'max': {
        '@id': 'http://trials.drugis.org/ontology#max',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'geometric_coefficient_of_variation': {
        '@id': 'http://trials.drugis.org/ontology#geometric_coefficient_of_variation',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'first_quartile': {
        '@id': 'http://trials.drugis.org/ontology#first_quartile',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'third_quartile': {
        '@id': 'http://trials.drugis.org/ontology#third_quartile',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'standard_error': {
        '@id': 'http://trials.drugis.org/ontology#standard_error',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'percentage': {
        '@id': 'http://trials.drugis.org/ontology#percentage',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'proportion': {
        '@id': 'http://trials.drugis.org/ontology#proportion',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'standard_deviation': {
        '@id': 'http://trials.drugis.org/ontology#standard_deviation',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'sample_size': {
        '@id': 'http://trials.drugis.org/ontology#sample_size',
        '@type': 'http://www.w3.org/2001/XMLSchema#integer'
      },
      'of_outcome': {
        '@id': 'http://trials.drugis.org/ontology#of_outcome',
        '@type': '@id'
      },
      'of_moment': {
        '@id': 'http://trials.drugis.org/ontology#of_moment',
        '@type': '@id'
      },
      'of_group': {
        '@id': 'http://trials.drugis.org/ontology#of_group',
        '@type': '@id'
      },
      'mean': {
        '@id': 'http://trials.drugis.org/ontology#mean',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'sameAs': {
        '@id': 'http://www.w3.org/2002/07/owl#sameAs',
        '@type': '@id'
      },
      'measurementType': {
        '@id': 'http://trials.drugis.org/ontology#measurementType',
        '@type': '@id'
      },
      'label': {
        '@id': 'http://www.w3.org/2000/01/rdf-schema#label',
        '@type': 'http://www.w3.org/2001/XMLSchema#string'
      },
      'comment': {
        '@id': 'http://www.w3.org/2000/01/rdf-schema#comment',
        '@type': 'http://www.w3.org/2001/XMLSchema#string'
      },
      'categoryList': {
        '@id': 'http://trials.drugis.org/ontology#categoryList',
        '@type': '@id'
      },
      'has_outcome': {
        '@id': 'http://trials.drugis.org/ontology#has_outcome',
        '@type': '@id'
      },
      'has_included_population': {
        '@id': 'http://trials.drugis.org/ontology#has_included_population',
        '@type': '@id'
      },
      'has_primary_epoch': {
        '@id': 'http://trials.drugis.org/ontology#has_primary_epoch',
        '@type': '@id'
      },
      'has_publication': {
        '@id': 'http://trials.drugis.org/ontology#has_publication',
        '@type': '@id'
      },
      'has_allocation': {
        '@id': 'http://trials.drugis.org/ontology#has_allocation',
        '@type': '@id'
      },
      'has_objective': {
        '@id': 'http://trials.drugis.org/ontology#has_objective',
        '@type': '@id'
      },
      'has_indication': {
        '@id': 'http://trials.drugis.org/ontology#has_indication',
        '@type': '@id'
      },
      'has_activity': {
        '@id': 'http://trials.drugis.org/ontology#has_activity',
        '@type': '@id'
      },
      'status': {
        '@id': 'http://trials.drugis.org/ontology#status',
        '@type': '@id'
      },
      'has_arm': {
        '@id': 'http://trials.drugis.org/ontology#has_arm',
        '@type': '@id'
      },
      'has_group': {
        '@id': 'http://trials.drugis.org/ontology#has_group',
        '@type': '@id'
      },
      'has_blinding': {
        '@id': 'http://trials.drugis.org/ontology#has_blinding',
        '@type': '@id'
      },
      'has_epochs': {
        '@id': 'http://trials.drugis.org/ontology#has_epochs',
        '@type': '@id'
      },
      'has_eligibility_criteria': {
        '@id': 'http://trials.drugis.org/ontology#has_eligibility_criteria',
        '@type': '@id'
      },
      'has_number_of_centers': {
        '@id': 'http://trials.drugis.org/ontology#has_number_of_centers',
        '@type': 'http://www.w3.org/2001/XMLSchema#integer'
      },
      'rest': {
        '@id': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest'
      },
      'nil': {
        '@id': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#nil'
      },
      'first': {
        '@id': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#first'
      },
      'of_variable': {
        '@id': 'http://trials.drugis.org/ontology#of_variable',
        '@type': '@id'
      },
      'is_measured_at': {
        '@id': 'http://trials.drugis.org/ontology#is_measured_at',
        '@type': '@id'
      },
      'has_result_property': {
        '@id': 'http://trials.drugis.org/ontology#has_result_property',
        '@type': '@id'
      },
      'count': {
        '@id': 'http://trials.drugis.org/ontology#count',
        '@type': 'http://www.w3.org/2001/XMLSchema#integer'
      },
      'has_drug_treatment': {
        '@id': 'http://trials.drugis.org/ontology#has_drug_treatment',
        '@type': '@id'
      },
      'has_activity_application': {
        '@id': 'http://trials.drugis.org/ontology#has_activity_application',
        '@type': '@id'
      },
      'category_count': {
        '@id': 'http://trials.drugis.org/ontology#category_count',
        '@type': '@id'
      },
      'duration': {
        '@id': 'http://trials.drugis.org/ontology#duration',
        '@type': 'http://www.w3.org/2001/XMLSchema#duration'
      },
      'unit': {
        '@id': 'http://trials.drugis.org/ontology#unit',
        '@type': '@id'
      },
      'dosingPeriodicity': {
        '@id': 'http://trials.drugis.org/ontology#dosingPeriodicity',
        '@type': 'http://www.w3.org/2001/XMLSchema#duration'
      },
      'value': {
        '@id': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#value',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'category': {
        '@id': 'http://trials.drugis.org/ontology#category',
        '@type': 'http://www.w3.org/2001/XMLSchema#string'
      },
      'has_id': {
        '@id': 'http://trials.drugis.org/ontology#has_id',
        '@type': '@id'
      },
      'time_offset': {
        '@id': 'http://trials.drugis.org/ontology#time_offset',
        '@type': 'http://www.w3.org/2001/XMLSchema#duration'
      },
      'relative_to_epoch': {
        '@id': 'http://trials.drugis.org/ontology#relative_to_epoch',
        '@type': '@id'
      },
      'relative_to_anchor': {
        '@id': 'http://trials.drugis.org/ontology#relative_to_anchor',
        '@type': '@id'
      },
      'applied_to_arm': {
        '@id': 'http://trials.drugis.org/ontology#applied_to_arm',
        '@type': '@id'
      },
      'applied_in_epoch': {
        '@id': 'http://trials.drugis.org/ontology#applied_in_epoch',
        '@type': '@id'
      },
      'participants_starting': {
        '@id': 'http://trials.drugis.org/ontology#participants_starting',
        '@type': 'http://www.w3.org/2001/XMLSchema#integer'
      },
      'in_epoch': {
        '@id': 'http://trials.drugis.org/ontology#in_epoch',
        '@type': '@id'
      },
      'treatment_dose': {
        '@id': 'http://trials.drugis.org/ontology#treatment_dose',
        '@type': '@id'
      },
      'treatment_min_dose': {
        '@id': 'http://trials.drugis.org/ontology#treatment_min_dose',
        '@type': '@id'
      },
      'treatment_max_dose': {
        '@id': 'http://trials.drugis.org/ontology#treatment_max_dose',
        '@type': '@id'
      },
      'treatment_has_drug': {
        '@id': 'http://trials.drugis.org/ontology#treatment_has_drug',
        '@type': '@id'
      },
      'conversionMultiplier': {
        '@id': 'http://qudt.org/schema/qudt#conversionMultiplier',
        '@type': 'http://www.w3.org/2001/XMLSchema#double'
      },
      'ontology': 'http://trials.drugis.org/ontology#',
      'registry': {
        '@id': 'http://trials.drugis.org/ontology#registry',
        '@type': '@id'
      },
      'registration_id': 'http://trials.drugis.org/ontology#registration_id',
      'uri': 'http://purl.org/ontology/bibo/uri'
    };

    return linkedData;
  }

  return improveJsonLd;
});
