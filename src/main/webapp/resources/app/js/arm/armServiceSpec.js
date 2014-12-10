'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the arm service', function() {

    var rootScope, q, testStore, httpBackend, armService, rdfStoreService,
      editArmWithCommentSparql, deleteArmSparql, graphAsText,
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
      editArmWithCommentSparql = xmlHTTP.responseText;
      xmlHTTP.open('GET', 'base/app/sparql/deleteArm.sparql', false);
      xmlHTTP.send(null);
      deleteArmSparql = xmlHTTP.responseText;

      xmlHTTP.open('GET', 'base/test_graphs/testStudyGraph.n3', false);
      xmlHTTP.send(null);
      graphAsText = xmlHTTP.responseText;

      mockStudyService.doQuery.and.callFake(function(query) {
        var defer = q.defer();
        console.log('query: ' + query);
        testStore.execute(query, function(success) {
          defer.resolve(success);
        });
        return defer.promise;
      });

      httpBackend.expectGET('app/sparql/editArmWithComment.sparql').respond(editArmWithCommentSparql);
      httpBackend.expectGET('app/sparql/deleteArm.sparql').respond(deleteArmSparql);
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

    describe('editArm with comment', function() {

      it('should edit the arm (with comment)', function(done) {
        var mockArm = {
          armURI: {
            value: 'http://trials.drugis.org/instances/arm1uuid'
          },
          label: {
            value: 'new arm label'
          },
          comment: {
            value: 'new arm comment'
          }
        };

        armService.editArm(mockArm).then(function() {
          testStore.execute(armsQuery, function(success, results) {
            expect(results.length).toEqual(2);
            expect(results[0].armURI.value).toEqual(mockArm.armURI.value);
            expect(results[0].label.value).toEqual(mockArm.label.value);
            expect(results[0].comment.value).toEqual(mockArm.comment.value);
            done();
          });
        });

        rootScope.$digest();

      });
    });

    describe('deleteArm', function() {

      it('should delete the arm', function(done) {
        var mockArm = {
          armURI: {
            value: 'http://trials.drugis.org/instances/arm1uuid'
          }
        };

        armService.deleteArm(mockArm).then(function() {
          testStore.execute(armsQuery, function(success, results) {
            expect(results.length).toEqual(1);
            done();
          });
        })

        rootScope.$digest();

      });

    });

  });
});
