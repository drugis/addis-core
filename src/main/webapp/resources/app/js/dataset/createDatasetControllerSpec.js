'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('create dataset controller', function() {
    var scope,
      mockDatasetResource = jasmine.createSpyObj('DatasetResource', ['save']),
      mockModalInstance = jasmine.createSpyObj('$modalInstance', ['close', 'dismiss']),
      mockSuccessCallback = jasmine.createSpy('successCallback'),
      mockState = jasmine.createSpyObj('$state', ['go']),
      mockDataset = {
        title: 'title'
      },
      datasetDeferred = {};
    mockDatasetResource.save.and.returnValue(mockDataset);

    beforeEach(module('trialverse.dataset'));

    beforeEach(inject(function($rootScope, $controller, $q) {
      scope = $rootScope;

      datasetDeferred = $q.defer();
      mockDataset.$promise = datasetDeferred.promise;

      $controller('CreateDatasetController', {
        $scope: scope,
        $state: mockState,
        $modalInstance: mockModalInstance,
        DatasetResource: mockDatasetResource,
        successCallback: mockSuccessCallback
      });

    }));
    it('should place createDataset on the scope', function() {
      expect(scope.createDataset).not.toBe(null);
    });

    describe('createDataset when called', function() {
      beforeEach(function() {
        scope.dataset = mockDataset;
        scope.createDataset();
      });
      it('should save the dataset to the DatasetResource', function() {
        expect(mockDatasetResource.save).toHaveBeenCalledWith(mockDataset);
      });
      it('should close the modalInstance', function() {
        expect(mockModalInstance.close).toHaveBeenCalled();
      });
      describe('and on successful dataset creation', function() {
        beforeEach(function() {
          datasetDeferred.resolve(mockDataset);
          scope.$apply();
        });
        it('should call the successCallback', function() {
          expect(mockSuccessCallback).toHaveBeenCalled();
        });
      })
    });

    it('should place cancel on the scope', function() {
      expect(scope.cancel).not.toBe(null);
    });

    describe('cancel', function() {
      it('should close the modalInstance', function() {
        scope.cancel();
        expect(mockModalInstance.close).toHaveBeenCalled();
      })
    })
  });
});
