'use strict';
define(['angular', 'angular-mocks'], function () {
  describe('dataset controller', function() {

    var scope,
    mockModal = jasmine.createSpyObj('$mock', ['open']),
    mockDatasetService = jasmine.createSpyObj('DatasetService', ['loadDatasets'])
    ;

    beforeEach(module('trialverse.dataset'));

    beforeEach(inject(function($rootScope, $controller) {
      scope = $rootScope;
      $controller('DatasetsController', {
        $scope: scope,
        $modal: mockModal,
        DatasetService: mockDatasetService
      });

    }));

    it('should place createDatasetDialog on the scope', function() {
      expect(scope.createDatasetDialog).not.toBe(null);
    });

    it('should call loadDatasets', function() {
      expect(mockDatasetService.loadDatasets).toHaveBeenCalled();
    })

    describe('createDatasetDialog', function() {
      it('should open a modal', function() {
        scope.createDatasetDialog();
        expect(mockModal.open).toHaveBeenCalled();
      })
    })

  });
});
