'use strict';
define(['lodash'], function(_) {
  function propertyValuePredicate(property, value) {
    return function(subjectWithTriples) {
      return subjectWithTriples[property] === value;
    }
  }

  function improveJsonLd(linkedData) {
    // fix property names (also forces data types)
    var contextPairs = _.filter(_.pairs(linkedData['@context']), function(pair) {
      return pair[1]['@type'] !== '@id';
    });
    var typedProperties = _.zipObject(
      _.map(contextPairs, function(pair) {
         return [pair[1]['@id'], pair[0]]
    }));
    linkedData['@graph'] = _.map(linkedData['@graph'], function(subjectWithTriples) {
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
        if(typeof value === 'string') {
          return _.reduce(_.pairs(prefixes), function(accum, pair) {
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
      if (typeof arr === "string") {
        arr = [arr];
      }
      subject[propertyName] = _.map(arr, findAndRemoveFromGraph);
    }

    function inlineList(subject, propertyName) {
      subject[propertyName] = _.map(subject[propertyName]['@list'], findAndRemoveFromGraph);
      linkedData['@context'][propertyName]['@container'] = '@list';
      linkedData['@context'][propertyName]['@type'] = undefined;
    }

    function inlineObjectsForSubjectsWithProperty(subjectList, propertyName) {
      var subjectsWithProperty = _.filter(subjectList, function(subjectWithTriples) { return subjectWithTriples[propertyName] });
      _.forEach(subjectsWithProperty, function(subject) {
        inlineObjects(subject, propertyName);
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
    inlineObjects(study, 'has_activity');
    inlineObjects(study, 'has_indication');
    inlineObjects(study, 'has_objective');
    inlineObjects(study, 'has_publication');
    inlineObjects(study, 'has_eligibility_criteria');
    inlineList(study, 'has_epochs');

    return linkedData;
  }

  return improveJsonLd;
});
