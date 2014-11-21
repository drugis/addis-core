'use strict';
define([], function() {
  var dependencies = [];

  var StudyService = function() {

    function createEmptyStudyJsonLD(uuid, study) {
      return {
        '@graph': [{
          '@id': 'study:' + uuid,
          '@type': 'ontology:Study',
          'label': study.shortName,
          'comment': study.title
        }],
        '@id': 'urn:x-arq:DefaultGraphNode',
        '@context': {
          'atc' : 'http://www.whocc.no/ATC2011/',
          'comment': 'http://www.w3.org/2000/01/rdf-schema#comment',
          'dataset': 'http://trials.drugis.org/datasets/',
          'dc': 'http://purl.org/dc/elements/1.1/',
          'label': 'http://www.w3.org/2000/01/rdf-schema#label',
          'ontology': 'http://trials.drugis.org/ontology#',
          'owl': 'http://www.w3.org/2002/07/owl#',
          'rdf': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
          'rdfs': 'http://www.w3.org/2000/01/rdf-schema#',
          'study': 'http://trials.drugis.org/studies/',
        }
      };
    }

    return {
      createEmptyStudyJsonLD: createEmptyStudyJsonLD
    };
  };

  return dependencies.concat(StudyService);
});
