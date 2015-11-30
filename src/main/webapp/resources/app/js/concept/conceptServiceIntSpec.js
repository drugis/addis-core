'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('concept service', function() {

    var scope, q,
      conceptService,
      jsonPromiseDefer;

    beforeEach(module('trialverse.concept'));

    beforeEach(inject(function($rootScope, $q, ConceptService) {
      scope = $rootScope;
      q = $q;
      conceptService = ConceptService;
      jsonPromiseDefer = q.defer();
      conceptService.loadJson(jsonPromiseDefer.promise);
    }));

    describe('queryItems', function() {
      beforeEach(function() {
        jsonPromiseDefer.resolve({
          '@graph': [{
            '@id': 'http://uris/concept1',
            '@type': 'http://trials.drugis.org/ontology#Variable',
            'http://www.w3.org/2000/01/rdf-schema#label': 'Weight Loss'
          }]
        });
      });

      it('should retrieve the concepts', function(done) {
        var expected = [{
          uri: 'http://uris/concept1',
          type: {
            uri: 'http://trials.drugis.org/ontology#Variable',
            label: 'Variable'
          },
          label: 'Weight Loss'
        }];
        conceptService.queryItems().then(function(result) {
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
            uri: 'http://trials.drugis.org/ontology#Variable'
          }
        };
        conceptService.addItem(newConcept).then(function() {
          conceptService.queryItems().then(function(result) {
            var concepts = result;
            expect(concepts.length).toBe(1);
            expect(concepts[0].label).toEqual(newConcept.label);
            expect(concepts[0].type.uri).toEqual(newConcept.type.uri);
            done();
          });
        });
        scope.$digest();
      });
    })
  });
});