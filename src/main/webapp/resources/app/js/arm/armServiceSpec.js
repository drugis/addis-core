'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the arm service', function() {

    var rootScope, q, testStore, httpBackend, armService, rdfStoreService, rawSparql, graphAsText,
      mockStudyService = jasmine.createSpyObj('StudyService', ['doQuery']);
    var armsQuery =
      ' prefix ontology: <http://trials.drugis.org/ontology#>' +
      ' prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>' +
      ' prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>' +
      ' select' +
      ' ?armURI ?label ?comment ' +
      ' where {' +
      '    ?armURI ' +
      '      rdf:type ontology:Arm ;' +
      '      rdfs:label ?label . ' +
      '     OPTIONAL { ?armURI rdfs:comment ?comment . } ' +
      '}';

    var originalTimeout;

    beforeEach(module('trialverse.util'));
    beforeEach(module('trialverse.arm'));

    beforeEach(function() {
      module('trialverse', function($provide) {
        $provide.value('StudyService', mockStudyService);
      });
    });

    beforeEach(inject(function($q, $rootScope, $httpBackend, ArmService, RdfStoreService) {
      var xmlHTTP = new XMLHttpRequest();

      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      armService = ArmService;
      rdfStoreService = RdfStoreService;

      xmlHTTP.open('GET', 'base/app/sparql/editArmWithComment.sparql', false);
      xmlHTTP.send(null);
      rawSparql = xmlHTTP.responseText;

      xmlHTTP.open('GET', 'base/test_graphs/testStudyGraph.txt', false);
      xmlHTTP.send(null);
      graphAsText = xmlHTTP.responseText;

      httpBackend.expectGET('app/sparql/editArmWithComment.sparql').respond(rawSparql);
      httpBackend.flush();

    }));

    beforeEach(function(done) {
      rdfStoreService.create(function(store) {
        testStore = store;
        testStore.load('text/n3', graphAsText, function(success, results) {
          console.log('test store loaded, ' + results + ' triples loaded');
          done();
        });
      });
    });

    describe('edit', function() {

      it('should do a query with replaced values', function(done) {
        var mockArm = {
            armURI: {
              value: 'http://trials.drugis.org/instances/4a58d0a0-3c45-474e-8926-0d1fb250e5ce'
            },
            label: {
              value: 'new arm label'
            },
            comment: {
              value: 'new arm comment'
            }
          };

        mockStudyService.doQuery.and.callFake(function(query) {
          var defer = q.defer();
          testStore.execute(query, function(success) {
            defer.resolve();
          });
          return defer.promise;
        });

        armService.edit(mockArm).then(function() {
          testStore.execute(armsQuery, function(success, results) {
            expect(results[0].armURI.value).toEqual(mockArm.armURI.value);
            expect(results[0].label.value).toEqual(mockArm.label.value);
            expect(results[0].comment.value).toEqual(mockArm.comment.value);
            done();
          });
        });

        rootScope.$digest();

      });
    });
  });
});
