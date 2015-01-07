'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the epoch service', function() {

    var rootScope, q, testStore, httpBackend, epochService, rdfStoreService,
      queryEpochs, queryAddEpoch, queryAddEpochToEndOfList, queryAddEpochComment,
      graphAsText,
      mockStudyService = jasmine.createSpyObj('StudyService', ['doModifyingQuery', 'doNonModifyingQuery']);

    beforeEach(module('trialverse.util'));
    beforeEach(module('trialverse.epoch'));

    beforeEach(function() {
      module('trialverse', function($provide) {
        $provide.value('StudyService', mockStudyService);
      });
    });

    beforeEach(inject(function($q, $rootScope, $httpBackend, EpochService, RdfStoreService) {
      var xmlHTTP = new XMLHttpRequest();

      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      epochService = EpochService;
      rdfStoreService = RdfStoreService;

      function loadAndExpectResource(resourceName) {
        xmlHTTP.open('GET', 'base/app/sparql/' + resourceName, false);
        xmlHTTP.send(null);
        var response = xmlHTTP.responseText;
        httpBackend.expectGET('app/sparql/' + resourceName).respond(response);
        return response;
      }

      queryEpochs = loadAndExpectResource('queryEpoch.sparql');
      queryAddEpoch = loadAndExpectResource('addEpoch.sparql');
      queryAddEpochComment = loadAndExpectResource('addEpochComment.sparql');
      queryAddEpochToEndOfList = loadAndExpectResource('addEpochToStudyEpochList.sparql');

      xmlHTTP.open('GET', 'base/test_graphs/epochsTestStudyGraph.ttl', false);
      xmlHTTP.send(null);
      graphAsText = xmlHTTP.responseText;

      mockStudyService.doModifyingQuery.and.callFake(function(query) {
        var defer = q.defer();
        testStore.execute(query, function(success) {
          defer.resolve(success);
        });
        return defer.promise;
      });

      mockStudyService.doNonModifyingQuery.and.callFake(function(query) {
        var defer = q.defer();
        testStore.execute(query, function(success, results) {
          defer.resolve(results);
        });
        return defer.promise;
      });


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

    describe('rdfstore debug', function() {

      it('show the graph for debug ', function(done) {

        testStore.graph(function() {
          done();
        });

        rootScope.$digest();

      });
    });

    xdescribe('queryEpoch', function() {

      it('should query the epoch', function(done) {

        epochService.queryItems().then(function(results) {
          expect(results.length).toBe(2);

          expect(results[0].uri.value).toBe('http://trials.drugis.org/instances/epoch1uuid');
          expect(results[0].label.value).toBe('epoch 1 label');
          expect(results[0].comment.value).toBe('epoch 1 comment');
          expect(results[0].duration).toBe(null);

          expect(results[1].uri.value).toBe('http://trials.drugis.org/instances/epoch2uuid');
          expect(results[1].label.value).toBe('epoch 2 label');
          expect(results[1].comment).toBe(null);
          // for rdfstore-js ^^duration bug, see trello issue for more information
          expect(results[1].duration.value).toBe('P7D');
          done();
        });

        rootScope.$digest();

      });
    });

    describe('addEpoch', function() {

      it('should add the epoch', function(done) {
        var xmlHTTP = new XMLHttpRequest();
        var mockEpoch = {
          UUID: {
            value: 'http://trials.drugis.org/instances/epoch1UUID'
          },
          label: 'new epoch label',
          comment: 'new epoch comment',
          duration: {
            numberOfPeriods: 13,
            periodType: {
              value: 'D',
              type: 'day',
              label: 'day(s)'
            }
          }
        };

        xmlHTTP.open('GET', 'base/app/sparql/getLastItemInEpochList.sparql', false);
        xmlHTTP.send(null);
        var getLastItemInEpochList = xmlHTTP.responseText

        epochService.addItem(mockEpoch).then(function() {

          epochService.queryItems().then(function(results) {
            // expect 2 + 1 new epoch
            expect(results.length).toBe(3);
            expect(results[2].duration.value).toBe('P13D');
            expect(results[2].comment.value).toBe(mockEpoch.comment);
            expect(results[2].label.value).toBe(mockEpoch.label);
            done();
          });

          testStore.execute(getLastItemInEpochList, function(success, results) {
            console.log(results);
            expect(results).not.toBeNull();
          });

        });

        rootScope.$digest();

      });
    });


  });
});
