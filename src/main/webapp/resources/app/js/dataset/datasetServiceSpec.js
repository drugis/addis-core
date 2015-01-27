'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('dataset service', function() {

    var remoteRdfStoreService,
      loadDatasetStoreDefer,
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

      var datasetService;

      beforeEach(inject(function($q, DatasetService) {
        datasetService = DatasetService;
        createDefer = $q.defer();
        loadDatasetStoreDefer = $q.defer();
        remoteRdfStoreService.create.and.returnValue(createDefer.promise);
        remoteRdfStoreService.load.and.returnValue(loadDatasetStoreDefer.promise);
      }));

      it('should load data', inject(function($rootScope) {
        var promise = datasetService.loadStore('any info');
        createDefer.resolve('graphURI');
        loadDatasetStoreDefer.resolve();
        $rootScope.$digest();
        expect(promise.$$state.status).toBe(1);
      }));

      describe('queryDatasetsOverview', function() {

        beforeEach(inject(function($q) {
          datasetService.loadStore('any info');
          executeQueryDefer = $q.defer();
          remoteRdfStoreService.executeQuery.and.returnValue(executeQueryDefer.promise);
        }));

        it('should show a list of datasets', inject(function($rootScope) {
          var expectedResult = 'list of dataset';
          var promise = datasetService.queryDatasetsOverview();
          executeQueryDefer.resolve(expectedResult);
          loadDatasetStoreDefer.resolve();
          createDefer.resolve('graphURI');
          $rootScope.$digest();

          expect(promise.$$state.status).toBe(1);
          expect(promise.$$state.value).toBe('list of dataset');

        }));
      });

      describe('queryDataset', function() {

        beforeEach(inject(function($q) {
          datasetService.loadStore('any info');
          executeQueryDefer = $q.defer();
          remoteRdfStoreService.executeQuery.and.returnValue(executeQueryDefer.promise);
        }));

        it('should not fail when a single result is returned', inject(function($rootScope) {
          var promise = datasetService.queryDataset();
          executeQueryDefer.resolve('single result');
          loadDatasetStoreDefer.resolve();
          createDefer.resolve('graphURI');
          $rootScope.$digest();
          expect(promise.$$state.value).toBe('single result');
        }));
      });

      describe('executeUpdate', function() {
        var executeUpdateDefer;

        beforeEach(inject(function($q) {
          datasetService.loadStore('any info');
          executeUpdateDefer = $q.defer();
          remoteRdfStoreService.executeUpdate.and.returnValue(executeUpdateDefer.promise);
        }));

        it('should execute the update', inject(function($rootScope) {
          var promise = datasetService.addStudyToDatasetGraph('datasetUUID', 'studyUUID');
          executeUpdateDefer.resolve(200);
          loadDatasetStoreDefer.resolve();
          createDefer.resolve('graphURI');
          $rootScope.$digest();
          expect(promise.$$state.value).toBe(200);
          expect(remoteRdfStoreService.executeUpdate).toHaveBeenCalled();
        }));
      });
    });

  });
});
