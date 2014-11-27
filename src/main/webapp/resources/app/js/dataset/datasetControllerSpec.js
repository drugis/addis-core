'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('dataset controller', function() {

    var scope,
      mockModal = jasmine.createSpyObj('$mock', ['open']),
      mockDatasetService = jasmine.createSpyObj('DatasetService', ['getDatasets']),
      mockDatasetsResult,
      mockDatasetsDeferred;

    beforeEach(module('trialverse.dataset'));

    beforeEach(inject(function($rootScope, $q, $controller) {
      scope = $rootScope;
      mockDatasetsDeferred = $q.defer();
      mockDatasetsResult = {
        promise: mockDatasetsDeferred.promise,
      };
      mockDatasetService.getDatasets.and.returnValue(mockDatasetsResult);

      $controller('DatasetsController', {
        $scope: scope,
        $modal: mockModal,
        DatasetService: mockDatasetService
      });
    }));

    it('should place createDatasetDialog on the scope', function() {
      expect(scope.createDatasetDialog).not.toBe(null);
    });

    it('should call getDatasets', function() {
      expect(mockDatasetService.getDatasets).toHaveBeenCalled();
    });

    describe('createDatasetDialog', function() {
      it('should open a modal', function() {
        scope.createDatasetDialog();
        expect(mockModal.open).toHaveBeenCalled();
      });
    });

    describe('when the datasetResource fulfils its promise', function() {
      it('the result should be placed on scope.datasets', inject(function($rootScope) {
        var mockDatasets = [{
          id: 1
        }, {
          id: 1
        }];
        mockDatasetsDeferred.resolve(mockDatasets);
        $rootScope.$digest();
        expect($rootScope.datasets).toBe(mockDatasets);
      }));
    });

  });
});
