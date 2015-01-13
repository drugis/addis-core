'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the arm service', function() {

    var rootScope, q, httpBackend, armService, studyService,
      queryArms, addArmQuery, addArmCommentQuery, editArmWithCommentSparql, editArmWithoutCommentSparql,
      deleteArmSparql, deleteHasArmSparql, graphAsText,
      mockStudyService = jasmine.createSpyObj('StudyService', ['doModifyingQuery']);
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
        studyService = jasmine.createSpyObj('StudyService', ['doModifyingQuery']);
        $provide.value('StudyService', studyService);
      });
    });

    beforeEach(inject(function($q, $rootScope, $httpBackend, ArmService) {
      var xmlHTTP = new XMLHttpRequest();

      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      armService = ArmService;
      

      xmlHTTP.open('GET', 'base/app/sparql/queryArm.sparql', false);
      xmlHTTP.send(null);
      queryArms = xmlHTTP.responseText;
      xmlHTTP.open('GET', 'base/app/sparql/addArmQuery.sparql', false);
      xmlHTTP.send(null);
      addArmQuery = xmlHTTP.responseText;
      xmlHTTP.open('GET', 'base/app/sparql/addArmCommentQuery.sparql', false);
      xmlHTTP.send(null);
      addArmCommentQuery = xmlHTTP.responseText;
      xmlHTTP.open('GET', 'base/app/sparql/editArmWithComment.sparql', false);
      xmlHTTP.send(null);
      editArmWithCommentSparql = xmlHTTP.responseText;

      xmlHTTP.open('GET', 'base/app/sparql/editArmWithoutComment.sparql', false);
      xmlHTTP.send(null);
      editArmWithoutCommentSparql = xmlHTTP.responseText;
      xmlHTTP.open('GET', 'base/app/sparql/deleteArm.sparql', false);
      xmlHTTP.send(null);
      deleteArmSparql = xmlHTTP.responseText;
      xmlHTTP.open('GET', 'base/app/sparql/deleteHasArm.sparql', false);
      xmlHTTP.send(null);
      deleteHasArmSparql = xmlHTTP.responseText;

      xmlHTTP.open('GET', 'base/test_graphs/testStudyGraph.ttl', false);
      xmlHTTP.send(null);
      graphAsText = xmlHTTP.responseText;

      httpBackend.expectGET('app/sparql/queryArm.sparql').respond(queryArms);
      httpBackend.expectGET('app/sparql/addArmQuery.sparql').respond(addArmQuery);
      httpBackend.expectGET('app/sparql/addArmCommentQuery.sparql').respond(addArmCommentQuery);
      httpBackend.expectGET('app/sparql/editArmWithComment.sparql').respond(editArmWithCommentSparql);
      httpBackend.expectGET('app/sparql/editArmWithoutComment.sparql').respond(editArmWithoutCommentSparql);
      httpBackend.expectGET('app/sparql/deleteArm.sparql').respond(deleteArmSparql);
      httpBackend.expectGET('app/sparql/deleteHasArm.sparql').respond(deleteHasArmSparql);
      httpBackend.flush();

    }));

    describe('addArm with comment', function() {

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

        var doModifyingQueryDefer = q.defer();
        studyService.doModifyingQuery.and.returnValue(doModifyingQueryDefer.promise);

        var resultPromise = armService.addItem(mockArm, 'studyUid');
        doModifyingQueryDefer.resolve(200);
        rootScope.$digest();

        expect(studyService.doModifyingQuery).toHaveBeenCalled();
        expect(studyService.doModifyingQuery.calls.count()).toEqual(2);
        done();
      });
    });

    describe('editArm without comment', function() {

      it('should edit the arm (without comment)', function(done) {

        studyService.doModifyingQuery.calls.reset();
        var mockArm = {
          armURI: {
            value: 'http://trials.drugis.org/instances/arm1uuid'
          },
          label: {
            value: 'new arm label'
          }
        };

        var doModifyingQueryDefer = q.defer();
        studyService.doModifyingQuery.and.returnValue(doModifyingQueryDefer.promise);

        var resultPromise = armService.addItem(mockArm, 'studyUid');
        doModifyingQueryDefer.resolve(200);
        rootScope.$digest();

        expect(studyService.doModifyingQuery).toHaveBeenCalled();
        expect(studyService.doModifyingQuery.calls.count()).toEqual(1);
        done();
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

        var doModifyingQueryDefer = q.defer();
        studyService.doModifyingQuery.and.returnValue(doModifyingQueryDefer.promise);

        var resultPromise = armService.editItem(mockArm, 'studyUid');
        doModifyingQueryDefer.resolve(200);
        rootScope.$digest();

        expect(studyService.doModifyingQuery).toHaveBeenCalled();
        expect(studyService.doModifyingQuery.calls.count()).toEqual(1);
        done();
      });
    });

    describe('editArm without comment', function() {

      it('should edit the arm (without comment)', function(done) {

        studyService.doModifyingQuery.calls.reset();
        var mockArm = {
          armURI: {
            value: 'http://trials.drugis.org/instances/arm1uuid'
          },
          label: {
            value: 'new arm label'
          }
        };

        var doModifyingQueryDefer = q.defer();
        studyService.doModifyingQuery.and.returnValue(doModifyingQueryDefer.promise);

        var resultPromise = armService.editItem(mockArm, 'studyUid');
        doModifyingQueryDefer.resolve(200);
        rootScope.$digest();

        expect(studyService.doModifyingQuery).toHaveBeenCalled();
        expect(studyService.doModifyingQuery.calls.count()).toEqual(1);
        done();
      });
    });

    describe('deleteArm', function() {

      it('should delete the arm', function(done) {
        studyService.doModifyingQuery.calls.reset();
        var mockArm = {
          armURI: {
            value: 'http://trials.drugis.org/instances/arm1uuid'
          },
          label: {
            value: 'new arm label'
          }
        };

        var doModifyingQueryDefer = q.defer();
        studyService.doModifyingQuery.and.returnValue(doModifyingQueryDefer.promise);

        var resultPromise = armService.deleteItem(mockArm);
        doModifyingQueryDefer.resolve(200);
        rootScope.$digest();

        expect(studyService.doModifyingQuery).toHaveBeenCalled();
        expect(studyService.doModifyingQuery.calls.count()).toEqual(2);
        done();

      });

    });

  });
});
