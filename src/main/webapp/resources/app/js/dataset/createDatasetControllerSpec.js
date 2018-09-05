'use strict';
define(['angular-mocks', './dataset'], function() {
  describe('create dataset controller', function() {
    var scope = {},
      mockDatasetResource = jasmine.createSpyObj('DatasetResource', ['save']),
      mockModalInstance = jasmine.createSpyObj('$modalInstance', ['close', 'dismiss']),
      mockCallback = jasmine.createSpy('callback'),
      excelImportServiceMock = jasmine.createSpyObj('ExcelImportService', ['createDatasetStudies', 'createDatasetConcepts']),
      mockDataset = {
        title: 'title'
      },
      datasetTitles = [],
      datasetDeferred = {},
      userUid = 'userUid',
      datasetUuid = 'uuid-1',
      versionUuid = 'version-1',
      stateParams = {
        userUid: userUid,
        datasetUuid: datasetUuid,
        versionUuid: versionUuid
      };
    mockDatasetResource.save.and.returnValue(mockDataset);



    beforeEach(angular.mock.module('trialverse.user'));

    beforeEach(angular.mock.inject(function($rootScope, $controller, $q) {
      scope = $rootScope;

      datasetDeferred = $q.defer();
      mockDataset.$promise = datasetDeferred.promise;

      $controller('CreateDatasetController', {
        $scope: scope,
        $stateParams: stateParams,
        $modalInstance: mockModalInstance,
        DatasetResource: mockDatasetResource,
        ExcelImportService: excelImportServiceMock,
        datasetTitles: datasetTitles,
        callback: mockCallback,
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
          expect(mockCallback).toHaveBeenCalled();
        });
      });
    });

    it('should place cancel on the scope', function() {
      expect(scope.cancel).not.toBe(null);
    });

    describe('cancel', function() {
      it('should close the modalInstance', function() {
        scope.cancel();
        expect(mockModalInstance.close).toHaveBeenCalled();
      });
    });
  });
});
