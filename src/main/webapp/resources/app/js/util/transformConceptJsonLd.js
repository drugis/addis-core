'use strict';
define([], function() {
  // Work on object as object is both cashe and promise
  function transformConceptJson(conceptJson) {
    // fix the 1-item case
    var context = conceptJson['@context'] || {};

    if (!conceptJson['@graph']) {
      delete conceptJson['@context'];
      conceptJson['@graph'] = [{
        '@id': conceptJson['@id'],
        '@type': conceptJson['@type'],
        'label': conceptJson.label,
        'comment': conceptJson.comment,
      }];
      delete conceptJson['@id'];
      delete conceptJson['@type'];
      delete conceptJson.label;
      delete conceptJson.comment;
      conceptJson['@context'] = context;
    }
    context.ontology = 'http://trials.drugis.org/ontology#';
    context.comment = 'http://www.w3.org/2000/01/rdf-schema#comment';
    context.label = 'http://www.w3.org/2000/01/rdf-schema#label';
    // standardize type URIs
    conceptJson['@graph'] = conceptJson['@graph'].map(function(item) {
      if (item['@type']) {
        item['@type'] = item['@type'].replace(context.ontology, 'ontology:');
      }
      return item;
    });
    return conceptJson;
  }
  return transformConceptJson;
});
