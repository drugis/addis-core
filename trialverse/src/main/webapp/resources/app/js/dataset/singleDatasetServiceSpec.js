'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('dataset service', function() {

    var datasetService,
      loadDatasetStoreDefer,
      executeQueryDefer;

    beforeEach(module('trialverse.dataset', function($provide) {
      datasetService = jasmine.createSpyObj('datasetService', [
        'loadStore', 'executeUpdate', 'executeQuery', 'getGraph'
      ]);
      $provide.value('DatasetService', datasetService);
    }));


    describe('loadStore', function() {

      var singleDatasetService;

      beforeEach(inject(function($q, SingleDatasetService) {
        singleDatasetService = SingleDatasetService;
        loadDatasetStoreDefer = $q.defer();
        datasetService.loadStore.and.returnValue(loadDatasetStoreDefer.promise);
      }));

      it('should load data', inject(function($rootScope) {
        var promise = singleDatasetService.loadStore('any info');
        loadDatasetStoreDefer.resolve();
        $rootScope.$digest();
        expect(promise.$$state.status).toBe(1);
      }));

      describe('queryDataset', function() {

        beforeEach(inject(function($q) {
          singleDatasetService.loadStore('any info');
          executeQueryDefer = $q.defer();
          datasetService.executeQuery.and.returnValue(executeQueryDefer.promise);
        }));

        it('should not fail when a single result is returned', inject(function($rootScope) {
          var promise = singleDatasetService.queryDataset();
          executeQueryDefer.resolve('single result');
          loadDatasetStoreDefer.resolve();
          $rootScope.$digest();
          expect(promise.$$state.value).toBe('single result');
        }));
      });

      describe('executeUpdate', function() {
        var executeUpdateDefer;

        beforeEach(inject(function($q) {
          singleDatasetService.loadStore('any info');
          executeUpdateDefer = $q.defer();
          datasetService.executeUpdate.and.returnValue(executeUpdateDefer.promise);
        }));

        it('should execute the update', inject(function($rootScope) {
          var promise = singleDatasetService.addStudyToDatasetGraph('datasetUUID', 'studyUUID');
          executeUpdateDefer.resolve(200);
          loadDatasetStoreDefer.resolve();
          $rootScope.$digest();
          expect(promise.$$state.value).toBe(200);
          expect(datasetService.executeUpdate).toHaveBeenCalled();
        }));
      });
    });

  });
});
