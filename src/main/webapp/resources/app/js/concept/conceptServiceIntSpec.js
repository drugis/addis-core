'use strict';
define(['angular', 'angular-mocks', 'testUtils'], function(angular, angularMocks, testUtils) {
  describe('concept service', function() {

    var scope, q,
      conceptService
      ;

    beforeEach(module('trialverse.concept', function($provide) {
      $provide.value('RemoteRdfStoreService', remoteRdfStoreServiceMock);
    }));

    beforeEach(inject(function($rootScope, $q, ConceptService) {
      scope = $rootScope;
      q = $q;
      conceptService = ConceptService;

    }));

    describe('queryItems', function() {

      it('should retrieve the concepts', function(done) {
        conceptService.queryItems().then(function(result) {
          var concepts = result;
          expect(concepts.length).toBe(3);
          expect(concepts[0].label).toBe('endpoint');
          expect(concepts[1].label).toBe('conc 2 the druggening');
          expect(concepts[2].label).toBe('conc 1');
          expect(concepts[0].type).toBe('http://trials.drugis.org/ontology#Variable');
          expect(concepts[1].type).toBe('http://trials.drugis.org/ontology#Drug');
          expect(concepts[2].type).toBe('http://trials.drugis.org/ontology#Drug');
          done();
        });
        scope.$digest();
      });
    });

    describe('addItem', function() {
      it('should add the new concept to the graph', function(done) {
        var newConcept = {
          title: 'added concept',
          type: {
            uri: 'http://trials.drugis.org/ontology#AdverseEvent'
          }
        };
        conceptService.addItem(newConcept).then(function() {
          conceptService.queryItems().then(function(result) {
            var concepts = result;
            expect(concepts.length).toBe(4);
            expect(concepts[1].label).toEqual(newConcept.title);
            expect(concepts[1].type).toEqual(newConcept.type.uri);
            done();
          });
        });
        scope.$digest();
      });
    })
  });
});
