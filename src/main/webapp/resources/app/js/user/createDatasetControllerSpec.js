'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('create dataset controller', function() {
    var scope,
      mockDatasetResource = jasmine.createSpyObj('DatasetResource', ['save']),
      mockModalInstance = jasmine.createSpyObj('$modalInstance', ['close', 'dismiss']),
      mockcallback = jasmine.createSpy('callback'),
      mockDataset = {
        title: 'title'
      },
      datasetDeferred = {},
      userUid = 'userUid',
      datasetUUID = 'uuid-1',
      versionUuid = 'version-1',
      stateParams = {
        userUid: userUid,
        datasetUUID: datasetUUID,
        versionUuid: versionUuid
      };
    mockDatasetResource.save.and.returnValue(mockDataset);

    beforeEach(module('trialverse.user'));

    beforeEach(inject(function($rootScope, $controller, $q) {
      scope = $rootScope;

      datasetDeferred = $q.defer();
      mockDataset.$promise = datasetDeferred.promise;

      $controller('CreateDatasetController', {
        $scope: scope,
        $stateParams: stateParams,
        $modalInstance: mockModalInstance,
        DatasetResource: mockDatasetResource,
        callback: mockcallback
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
        expect(mockDatasetResource.save).toHaveBeenCalledWith(stateParams, mockDataset);
      });
      it('should close the modalInstance', function() {
        expect(mockModalInstance.close).toHaveBeenCalled();
      });
      describe('and on successful dataset creation', function() {
        beforeEach(function() {
          datasetDeferred.resolve(mockDataset);
          scope.$apply();
        });
        it('should call the callback', function() {
          expect(mockcallback).toHaveBeenCalled();
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
