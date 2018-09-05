'use strict';
define(['angular-mocks', './dataset', '../user/user'], function(angularMocks) {
  describe('datasets controller', function() {

    var scope,
      mockModal = jasmine.createSpyObj('$mock', ['open']),
      pageTitleServiceMock = jasmine.createSpyObj('PageTitleService', ['setPageTitle']),
      mockState = {
        params: {}
      };

    beforeEach(angular.mock.module('trialverse.dataset'));

    beforeEach(inject(function($rootScope, $q, $controller, DatasetResource) {
      scope = $rootScope;

      $controller('DatasetsController', {
        $scope: scope,
        $modal: mockModal,
        $state: mockState,
        $stateParams: {userUid: 1},
        DatasetResource: DatasetResource, 
        PageTitleService: pageTitleServiceMock
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
