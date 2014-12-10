'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the arm service', function() {

    var mockStudyService = jasmine.createSpyObj('StudyService', ['doQuery']);
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

    beforeEach(module('trialverse.util'));
    beforeEach(module('trialverse.arm'));

    beforeEach(function() {
      module('trialverse', function($provide) {
        $provide.value('StudyService', mockStudyService);
      });
    });


    describe('edit', function() {
      it('should do a query with replaced values', inject(function($rootScope, $httpBackend, ArmService, RdfStoreService) {

        var xmlHTTP = new XMLHttpRequest();
        xmlHTTP.open('GET', 'base/app/sparql/editArmWithComment.sparql', false);
        xmlHTTP.send(null);
        var rawSparql = xmlHTTP.responseText;

        xmlHTTP.open('GET', 'base/test_graphs/testStudyGraph.txt', false);
        xmlHTTP.send(null);
        var graphAsText = xmlHTTP.responseText;

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

        $httpBackend.expectGET('app/sparql/editArmWithComment.sparql').respond(rawSparql);

        mockStudyService.doQuery.and.callFake(function(query) {
          testStore.execute(query, function(success) {
            console.log('edit complete');
          });
        });

        var testStore;
        var resultArm;
        RdfStoreService.create(function(store) {
          testStore = store;
          testStore.load('text/n3', graphAsText, function(success, results) {
            console.log('test store loaded, ' + results + 'triples loaded');

            $httpBackend.flush();
            $rootScope.$digest();

            ArmService.edit(mockArm);

            $rootScope.$digest();

            testStore.execute(armsQuery, function(success, results) {
              console.log("new arms are :");
              angular.forEach(results, function(arm) {
                console.log(arm.armURI.value);
                console.log(arm.label.value);
                console.log(arm.comment.value);
              });
              resultArm = results[0];
              checkResult();

            });

          });
        });

        function checkResult() {
          expect(resultArm.armURI.value).toEqual(mockArm.armURI.value);
          expect(resultArm.label.value).toEqual(mockArm.label.value);
          expect(resultArm.comment.value).toEqual(mockArm.comment.value);
        }

      }));
    });
  });
});