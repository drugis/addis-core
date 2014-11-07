'use strict';
define(['angular', 'angular-mocks'], function () {
  describe('dataset controller', function() {

    var scope, mockModal = jasmine.createSpyObj('$mock', ['open']);

    beforeEach(module('trialverse.dataset'));

    beforeEach(inject(function($rootScope, $controller) {
      scope = $rootScope;
      $controller('DatasetsController', {
        $scope: scope,
        $modal: mockModal
      });

    }));

    it('should place createDatasetDialog on the scope', function() {
      expect(scope.createDatasetDialog).not.toBe(null);
    });

    describe('createDatasetDialog', function() {
      it('should open a modal', function() {
        scope.createDatasetDialog();
        expect(mockModal.open).toHaveBeenCalled();
      })
    })

  });
});
