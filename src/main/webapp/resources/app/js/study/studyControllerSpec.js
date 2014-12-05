'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the study controller', function() {

    var scope,
      mockStateParams = {
        datasetUUID: 'datasetUUID',
        studyUUID: 'studyUUID'
      },
      mockAnchorScroll = jasmine.createSpy('anchorScroll'),
      mockLocation = jasmine.createSpyObj('location', ['hash']),
      mockModal = jasmine.createSpyObj('modal', ['open']),
      mockStudyService = jasmine.createSpyObj('StudyService', ['queryArmData', 'loadStore', 'exportGraph']);

    beforeEach(module('trialverse.study'));

    beforeEach(inject(function($rootScope, $controller, StudyResource) {
      scope = $rootScope;

      $controller('StudyController', {
        $scope: scope,
        $stateParams: mockStateParams,
        StudyResource: { get: function() {}},
        $location: mockLocation,
        $anchorScroll: mockAnchorScroll,
        $modal: mockModal,
        StudyService: mockStudyService
      });
    }));

    describe('on load', function() {

      it('should place study and arms on the scope', function() {
        expect(scope.study).toBeDefined();
        expect(scope.arms).toBeDefined();
      });

    });

  });
});
