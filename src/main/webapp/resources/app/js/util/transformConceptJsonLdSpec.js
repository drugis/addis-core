'use strict';
define(['./transformConceptJsonLd'], function(transformConceptJsonLd) {
  describe('transformConceptJsonLd', function() {
    it('should work for an empty concepts graph', function() {
      var emptyData = {
        '@context': undefined,
        '@graph': []
      };
      expect(transformConceptJsonLd(emptyData)).toEqual(emptyData);
    });
    it('should work for concepts including categoricals (which are left untransformed)', function() {
      var data = {
        '@graph': [{
          '@id': 'http://localhost:8080/.well-known/genid/00000152697798d7e849192900000001',
          'first': 'Male',
          'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest': {
            '@list': ['Female']
          }
        }, {
          '@id': 'http://trials.drugis.org/concepts/02c0e50f-c856-4919-90f6-fc31f9b51b4a',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Agitation'
        }, {
          '@id': 'http://trials.drugis.org/concepts/7af6e330-0a60-4d01-bfe8-63905965fafa',
          '@type': 'http://trials.drugis.org/ontology#Variable',
          'categoryList': 'http://localhost:8080/.well-known/genid/00000152697798d7e849192900000001',
          'measurementType': 'http://trials.drugis.org/ontology#categorical',
          'comment': '',
          'label': 'Sex'
        },  {
          '@id': 'http://trials.drugis.org/concepts/fae9a4c4-a048-4b4c-a15a-c3bd8d2ba716',
          '@type': 'http://trials.drugis.org/ontology#Drug',
          'label': 'Duloxetine',
          'sameAs': 'http://www.whocc.no/ATC2011/N06AX21'
        }, {
          '@id': 'http://trials.drugis.org/concepts/fcb5fb34-a2cb-4766-8ff7-d14fe455786d',
          '@type': 'http://www.w3.org/2002/07/owl#Class',
          'symbol': 'g',
          'label': 'gram',
          'sameAs': 'http://qudt.org/schema/qudt#Gram'
        }],
        '@context': {
          'sameAs': {
            '@id': 'http://www.w3.org/2002/07/owl#sameAs',
            '@type': '@id'
          },
          'label': 'http://www.w3.org/2000/01/rdf-schema#label',
          'measurementType': {
            '@id': 'http://trials.drugis.org/ontology#measurementType',
            '@type': '@id'
          },
          'comment': 'http://www.w3.org/2000/01/rdf-schema#comment',
          'symbol': 'http://qudt.org/schema/qudt#symbol',
          'categoryList': {
            '@id': 'http://trials.drugis.org/ontology#categoryList',
            '@type': '@id'
          },
          'rest': {
            '@id': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest',
            '@type': '@id'
          },
          'first': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#first'
        }
      };

      var expected = {
        '@graph': [{
          '@id': 'http://localhost:8080/.well-known/genid/00000152697798d7e849192900000001',
          'first': 'Male',
          'http://www.w3.org/1999/02/22-rdf-syntax-ns#rest': {
            '@list': ['Female']
          }
        }, {
          '@id': 'http://trials.drugis.org/concepts/02c0e50f-c856-4919-90f6-fc31f9b51b4a',
          '@type': 'ontology:Variable',
          'measurementType': 'http://trials.drugis.org/ontology#dichotomous',
          'comment': '',
          'label': 'Agitation'
        }, {
          '@id': 'http://trials.drugis.org/concepts/7af6e330-0a60-4d01-bfe8-63905965fafa',
          '@type': 'ontology:Variable',
          'categoryList': 'http://localhost:8080/.well-known/genid/00000152697798d7e849192900000001',
          'measurementType': 'http://trials.drugis.org/ontology#categorical',
          'comment': '',
          'label': 'Sex'
        }, {
          '@id': 'http://trials.drugis.org/concepts/fae9a4c4-a048-4b4c-a15a-c3bd8d2ba716',
          '@type': 'ontology:Drug',
          'label': 'Duloxetine',
          'sameAs': 'http://www.whocc.no/ATC2011/N06AX21'
        }, {
          '@id': 'http://trials.drugis.org/concepts/fcb5fb34-a2cb-4766-8ff7-d14fe455786d',
          '@type': 'http://www.w3.org/2002/07/owl#Class',
          'symbol': 'g',
          'label': 'gram',
          'sameAs': 'http://qudt.org/schema/qudt#Gram'
        }],
        '@context': data['@context']
      };

      expect(transformConceptJsonLd(data)).toEqual(expected);

    });
  });
});
