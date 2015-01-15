'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the epoch service', function() {

    var rootScope, q, testStore, httpBackend, epochService,
      queryEpochs, queryAddEpoch, queryAddEpochToEndOfList, queryAddEpochComment,
      editEpoch, addEpochToEndOfList, setEpochPrimary,
      graphAsText, getLastItemInEpochList, deleteTail, nonModifyingqueryPromise,
      studyService = jasmine.createSpyObj('StudyService', ['doModifyingQuery', 'doNonModifyingQuery']);

    beforeEach(module('trialverse.util'));
    beforeEach(module('trialverse.epoch'));

    beforeEach(function() {
      module('trialverse', function($provide) {
        $provide.value('StudyService', studyService);
      });
    });

    beforeEach(inject(function($q, $rootScope, $httpBackend, EpochService) {
      var xmlHTTP = new XMLHttpRequest();

      q = $q;
      httpBackend = $httpBackend;
      rootScope = $rootScope;
      epochService = EpochService;
      nonModifyingqueryPromise = $q.defer();

      studyService.doNonModifyingQuery.and.returnValue(nonModifyingqueryPromise);

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
      addEpochToEndOfList = loadAndExpectResource('addEpochToEndOfList.sparql');
      setEpochPrimary = loadAndExpectResource('setEpochPrimary.sparql');
      editEpoch = loadAndExpectResource('editEpoch.sparql');

      httpBackend.flush();

    }));


    describe('queryItems', function() {

      it('should query the epochs', function() {
        var result = epochService.queryItems();
        var queryResult = 'bla';

        nonModifyingqueryPromise.resolve(queryResult);

        rootScope.$digest();

        expect(result.$$state.value.promise.$$state.value).toEqual(queryResult);

      });
    });

    describe('addEpoch', function() {

      it('should add the epoch with comment and set primary and add it to the list', function() {
        var mockEpoch = {
          label: 'new epoch label',
          comment: 'new epoch comment',
          isPrimaryEpoch: true,
          duration: {
            numberOfPeriods: 13,
            periodType: {
              value: 'D',
              type: 'day',
              label: 'day(s)'
            }
          }
        };
        studyService.doModifyingQuery.calls.reset();
        epochService.addItem(mockEpoch);

        rootScope.$digest();

        expect(studyService.doModifyingQuery.calls.count()).toEqual(4);

      });

      it('should add the epoch without comment', function() {
        var mockEpoch = {
          label: 'new epoch label',
          duration: {
            numberOfPeriods: 13,
            periodType: {
              value: 'D',
              type: 'day',
              label: 'day(s)'
            }
          }
        };
        studyService.doModifyingQuery.calls.reset();

        epochService.addItem(mockEpoch);

        rootScope.$digest();

        expect(studyService.doModifyingQuery.calls.count()).toEqual(1);

      });


    });


    describe('editEpoch', function() {
      it('should modify the epoch without comment', function() {
        var mockEpoch = {
          uri: {
            value: 'uri'
          },
          measurementType: {
            value: 'measType'
          },
          label: 'new epoch label',
          duration: {
            numberOfPeriods: 13,
            periodType: {
              value: 'D',
              type: 'day',
              label: 'day(s)'
            }
          }
        };
        studyService.doModifyingQuery.calls.reset();

        epochService.editItem(mockEpoch);

        rootScope.$digest();

        expect(studyService.doModifyingQuery.calls.count()).toEqual(1);

      });
    });

  });
});
