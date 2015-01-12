'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('dataset service', function() {

    var remoteRdfStoreService,
      loadDefer,
      createDefer,
      executeQueryDefer,
      queryResult = 'queryResult',
      scratchUri = 'graphURI';

    var newStore = {
      load: function(arg1, arg2, callback) {
        callback(true, 'resultString');
      },
      execute: function(queryArg, callbackArg) {
        callbackArg(true, queryResult);
      }
    };

    beforeEach(module('trialverse.dataset', function($provide) {
      remoteRdfStoreService = jasmine.createSpyObj('RemoteRdfStoreService', [
        'create', 'load', 'executeUpdate', 'executeQuery', 'getGraph'
      ]);
      $provide.value('RemoteRdfStoreService', remoteRdfStoreService);
    }));


    describe('loadStore', function() {

      beforeEach(inject(function($q) {
        createDefer = $q.defer();
        loadDefer = $q.defer();
        remoteRdfStoreService.create.and.returnValue(createDefer.promise);
        remoteRdfStoreService.load.and.returnValue(loadDefer.promise);
      }));

      it('should load data', inject(function(DatasetService, $rootScope) {
        var promise = DatasetService.loadStore('any info');
        createDefer.resolve('graphURI');
        loadDefer.resolve('resultString');
        $rootScope.$digest();
        expect(promise.$$state.value).toBe('resultString');
      }));

      describe('queryDatasetsOverview', function() {

        beforeEach(inject(function($q) {
          executeQueryDefer = $q.defer();
          remoteRdfStoreService.executeQuery.and.returnValue(executeQueryDefer.promise);
        }));

        it('should show a list of datasets', inject(function(DatasetService, $rootScope) {
          var promise = DatasetService.queryDatasetsOverview();
          executeQueryDefer.resolve('list of datasets');
          $rootScope.$digest();
          expect(promise.$$state.value).toBe('list of datasets');
        }))
      });

      describe('queryDataset', function() {

        beforeEach(inject(function($q) {
          executeQueryDefer = $q.defer();
          remoteRdfStoreService.executeQuery.and.returnValue(executeQueryDefer.promise);
        }));

        it('should not fail when a single result is returned', inject(function(DatasetService, $rootScope) {
          var promise = DatasetService.queryDataset();
          executeQueryDefer.resolve('single result');
          $rootScope.$digest();
          expect(promise.$$state.value).toBe('single result');
        }));
      });

      describe('executeUpdate', function() {
        var executeUpdateDefer;

        beforeEach(inject(function($q) {
          executeUpdateDefer = $q.defer();
          remoteRdfStoreService.executeUpdate.and.returnValue(executeUpdateDefer.promise);
        }));

        it('should execute the update', inject(function(DatasetService, $rootScope) {
          var promise = DatasetService.addStudyToDatasetGraph('datasetUUID', 'studyUUID');
          executeUpdateDefer.resolve(200);
          $rootScope.$digest();
          expect(promise.$$state.value).toBe(200);
          expect(remoteRdfStoreService.executeUpdate).toHaveBeenCalled();
        }));
      });
    });

  });
});