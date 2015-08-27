'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('dataset service', function() {

    var datasetService,
      loadDatasetStoreDefer,
      executeQueryDefer;

    beforeEach(module('trialverse.dataset', function($provide) {
      datasetService = jasmine.createSpyObj('datasetService', [
        'loadStore', 'executeQuery'
      ]);
      $provide.value('DatasetService', datasetService);
    }));


    describe('loadStore', function() {

      var datasetOverviewService;

      beforeEach(inject(function($q, DatasetOverviewService) {
        datasetOverviewService = DatasetOverviewService;
        loadDatasetStoreDefer = $q.defer();
        datasetService.loadStore.and.returnValue(loadDatasetStoreDefer.promise);
      }));

      it('should load data', inject(function($rootScope) {
        var promise = datasetOverviewService.loadStore('any info');
        loadDatasetStoreDefer.resolve();
        $rootScope.$digest();
        expect(promise.$$state.status).toBe(1);
      }));

      describe('queryDatasetsOverview', function() {

        beforeEach(inject(function($q) {
          datasetOverviewService.loadStore('any info');
          executeQueryDefer = $q.defer();
          datasetService.executeQuery.and.returnValue(executeQueryDefer.promise);
        }));

        it('should show a list of datasets', inject(function($rootScope) {
          var expectedResult = 'list of dataset';
          var promise = datasetOverviewService.queryDatasetsOverview();
          executeQueryDefer.resolve(expectedResult);
          loadDatasetStoreDefer.resolve();
          $rootScope.$digest();

          expect(promise.$$state.status).toBe(1);
          expect(promise.$$state.value).toBe('list of dataset');

        }));
      });
    });
  });
});
