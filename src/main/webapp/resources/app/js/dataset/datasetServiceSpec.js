'use strict';
define(['angular', 'angular-mocks', 'rdfstore'], function(rdfstore) {
  describe('dataset service', function() {

    var queryResult = 'queryResult';

    var newStore = {
      load: function(arg1, arg2, callback) {
        callback(true, 'resultString');
      },
      execute: function(queryArg, callbackArg) {
        callbackArg(true, queryResult);
      }
    };

    var rdfstoreService = {
      create: function(callback) {
        callback(newStore);
      }
    };


    beforeEach(module('trialverse.dataset', function($provide) {
      $provide.value('RdfstoreService', rdfstoreService);
    }));


    describe('loadStore', function() {

      it('should load data', inject(function(DatasetService, $rootScope) {
        var promise = DatasetService.loadStore('any info');
        $rootScope.$digest();
        expect(promise.$$state.value).toBe('resultString');
      }));
    });

    describe('queryDatasetsOverview', function() {
      it('should show a list of datasets', inject(function(DatasetService, $rootScope) {
        DatasetService.loadStore('any info');
        var promise = DatasetService.queryDatasetsOverview();
        $rootScope.$digest();
        expect(promise.$$state.value).toBe(queryResult);
      }))
    });

    describe('queryDataset', function() {

      it('should fail when multiple result are returned', inject(function(DatasetService, $rootScope) {
        queryResult = [1, 2, 3];
        DatasetService.loadStore('any info');
        var promise = DatasetService.queryDataset();
        $rootScope.$digest();
        expect(promise.$$state.status).toEqual(2);
      }));

      it('should not fail when a single result is returned', inject(function(DatasetService, $rootScope) {
        queryResult = ['single result'];
        DatasetService.loadStore('any info');
        var promise = DatasetService.queryDataset();
        $rootScope.$digest();
        expect(promise.$$state.status).toBe(1);
        expect(promise.$$state.value).toBe(queryResult[0]);
      }));

    });
  });
});