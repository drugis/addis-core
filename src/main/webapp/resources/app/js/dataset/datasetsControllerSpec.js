'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('datasets controller', function() {

    var scope, httpBackend,
      mockModal = jasmine.createSpyObj('$mock', ['open']),
      mockDatasetService = jasmine.createSpyObj('DatasetService', ['loadStore', 'queryDatasetsOverview']),
      mockLoadStoreDeferred,
      mockQueryDatasetsDeferred;

    beforeEach(module('trialverse.dataset'));


    beforeEach(inject(function($rootScope, $q, $controller, $httpBackend, DatasetResource) {
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
        DatasetResource: DatasetResource,
        DatasetService: mockDatasetService
      });

    }));

    it('should place createDatasetDialog on the scope', function() {
      expect(scope.createDatasetDialog).not.toBe(null);
    });

    it('should query the datasetResource, process the results and place them on the scope', function() {
      httpBackend.flush();

      expect(mockDatasetService.loadStore).toHaveBeenCalled();
      mockLoadStoreDeferred.resolve(101);
      scope.$digest();
      expect(mockDatasetService.queryDatasetsOverview).toHaveBeenCalled();
      var mockResults = {
        data: {
          results: {
            bindings: "my result"
          }
        }
      };
      mockQueryDatasetsDeferred.resolve(mockResults);
      scope.$digest();
      expect(scope.datasets).toBe('my result');
    });

    describe('createDatasetDialog', function() {
      it('should open a modal', function() {
        scope.createDatasetDialog();
        expect(mockModal.open).toHaveBeenCalled();
      });
    });

  });
});