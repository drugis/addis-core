'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('datasets controller', function() {

    var scope,
      mockModal = jasmine.createSpyObj('$mock', ['open']);

    beforeEach(module('trialverse.user'));

    beforeEach(angularMocks.inject(function($rootScope, $q, $controller, DatasetResource) {
      scope = $rootScope;

      $controller('DatasetsController', {
        $scope: scope,
        $modal: mockModal,
        $stateParams: {userUid: 1},
        DatasetResource: DatasetResource
      });

    }));

    it('should place createDatasetDialog on the scope', function() {
      expect(scope.createDatasetDialog).not.toBe(null);
    });


    describe('createDatasetDialog', function() {
      it('should open a modal', function() {
        scope.createDatasetDialog();
        expect(mockModal.open).toHaveBeenCalled();
      });
    });

  });
});
