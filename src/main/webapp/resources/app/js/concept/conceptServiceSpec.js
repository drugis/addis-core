'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('concept service', function() {

    var scope, q,
      conceptsService,
      jsonPromiseDefer;

    beforeEach(angular.mock.module('trialverse.concept'));

    beforeEach(inject(function($rootScope, $q, ConceptsService) {
      scope = $rootScope;
      q = $q;
      conceptsService = ConceptsService;
      jsonPromiseDefer = q.defer();
      conceptsService.loadJson(jsonPromiseDefer.promise);
    }));

    describe('queryItems', function() {
      beforeEach(function() {
        jsonPromiseDefer.resolve({
          '@graph': [{
            '@id': 'http://uris/concept1',
            '@type': 'ontology:Variable',
            label: 'Weight Loss',
            categoryList: 'http://wellknown.blankNode'
          }, {
            '@id': 'http://wellknown.blankNode',
            first: 'a lot',
            rest: 'http://rdf.nil'
          }]
        });
      });

      it('should retrieve the concepts', function(done) {
        var expected = [{
          uri: 'http://uris/concept1',
          type: {
            uri: 'ontology:Variable',
            label: 'Variable'
          },
          label: 'Weight Loss'
        }];
        conceptsService.queryItems().then(function(result) {
          expect(result).toEqual(expected);
          done();
        });
        scope.$digest();
      });
    });

    describe('addItem', function() {
      beforeEach(function() {
        jsonPromiseDefer.resolve({
          '@graph': []
        });
      });
      it('should add the new concept to the graph', function(done) {
        var newConcept = {
          label: 'added concept',
          type: {
            label: 'Variable',
            uri: 'ontology:Variable'
          }
        };
        conceptsService.addItem(newConcept).then(function() {
          conceptsService.queryItems().then(function(result) {
            var concepts = result;
            expect(concepts.length).toBe(1);
            expect(concepts[0].label).toEqual(newConcept.label);
            expect(concepts[0].type.uri).toEqual(newConcept.type.uri);
            done();
          });
        });
        scope.$digest();
      });
    });
  });
});