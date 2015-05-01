'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('datasets controller', function() {

    var scope, httpBackend,
      mockModal = jasmine.createSpyObj('$mock', ['open']),
      mockDatasetService = jasmine.createSpyObj('DatasetService', ['loadStore', 'queryDatasetsOverview', 'reset']),
      mockLoadStoreDeferred,
      mockQueryDatasetsDeferred;

    beforeEach(module('trialverse.user'));


    beforeEach(inject(function($rootScope, $q, $controller, $httpBackend, DatasetVersionedResource) {
      scope = $rootScope;
      httpBackend = $httpBackend;

      mockLoadStoreDeferred = $q.defer();
      mockQueryDatasetsDeferred = $q.defer();

      mockDatasetService.loadStore.and.returnValue(mockLoadStoreDeferred.promise);
      mockDatasetService.queryDatasetsOverview.and.returnValue(mockQueryDatasetsDeferred.promise);

      httpBackend.expectGET('/datasets').respond('datasets');

      $controller('DatasetsController', {
        $scope: scope,
        $modal: mockModal,
        $stateParams: {userUid: 12345},
        DatasetVersionedResource: DatasetVersionedResource,
        DatasetService: mockDatasetService
      });

    }));

    it('should place createDatasetDialog on the scope', function() {
      expect(scope.createDatasetDialog).not.toBe(null);
    });

    it('should query the datasetVersionedResource, process the results and place them on the scope', function() {
      var datasets = [{title: 'my results', headVersion: 'http://host/versions/foo'}];
      httpBackend.flush();

      expect(mockDatasetService.loadStore).toHaveBeenCalled();
      mockLoadStoreDeferred.resolve(101);
      scope.$digest();
      expect(mockDatasetService.queryDatasetsOverview).toHaveBeenCalled();
      mockQueryDatasetsDeferred.resolve(datasets);
      scope.$digest();
      expect(scope.datasets).toBe(datasets);
    });

    describe('createDatasetDialog', function() {
      it('should open a modal', function() {
        scope.createDatasetDialog();
        expect(mockModal.open).toHaveBeenCalled();
      });
    });

  });
});
