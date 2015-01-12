'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the study controller', function() {

    var scope, httpBackend,
      datasetUUID = 'datasetUUID',
      studyUUID = 'studyUUID',
      mockStateParams = {
        datasetUUID: datasetUUID,
        studyUUID: studyUUID
      },
      mockAnchorScroll = jasmine.createSpy('anchorScroll'),
      mockLocation = jasmine.createSpyObj('location', ['hash']),
      mockModal = jasmine.createSpyObj('modal', ['open']),
      mockStudyService = jasmine.createSpyObj('StudyService', ['reset','queryArmData', 'loadStore', 'queryStudyData', 'getStudyGraph', 'studySaved']),
      loadStoreDeferred,
      queryStudyDataDeferred,
      queryArmDataDeferred,
      getStudyGraphDeferred;

    beforeEach(module('trialverse.study'));

    beforeEach(inject(function($rootScope, $q, $controller, $httpBackend, StudyResource) {

      scope = $rootScope;
      httpBackend = $httpBackend;

      httpBackend.expectGET('/datasets/' + datasetUUID + '/studies/' + studyUUID).respond('study');

      loadStoreDeferred = $q.defer();
      queryStudyDataDeferred = $q.defer();
      queryArmDataDeferred = $q.defer();
      getStudyGraphDeferred = $q.defer();

      mockStudyService.loadStore.and.returnValue(loadStoreDeferred.promise);
      mockStudyService.queryStudyData.and.returnValue(queryStudyDataDeferred.promise);
      mockStudyService.queryArmData.and.returnValue(queryArmDataDeferred.promise);
      mockStudyService.getStudyGraph.and.returnValue(getStudyGraphDeferred.promise);

      $controller('StudyController', {
        $scope: scope,
        $stateParams: mockStateParams,
        StudyResource: StudyResource,
        $location: mockLocation,
        $anchorScroll: mockAnchorScroll,
        $modal: mockModal,
        StudyService: mockStudyService
      });
    }));

    describe('on load', function() {

      it('should place study and arms on the scope', function() {
        var
          studyQueryResult = 'studyQueryResult',
          armQueryResult = 'armQueryResult';

        expect(scope.study).toBeDefined();
        expect(scope.arms).toBeDefined();
        httpBackend.flush();
        expect(mockStudyService.loadStore).toHaveBeenCalled();
        loadStoreDeferred.resolve(3);
        scope.$digest();
        expect(mockStudyService.queryStudyData).toHaveBeenCalled();
        queryStudyDataDeferred.resolve(studyQueryResult);
        scope.$digest();
        expect(scope.study).toBe(studyQueryResult);
      });

    });

    describe('saveStudy', function() {
      it('should export the graph and PUT it to the resource', inject(function(StudyResource) {
        spyOn(StudyResource, 'put');
        scope.getStudyGraph();
        getStudyGraphDeferred.resolve();
        scope.$digest();
        expect(StudyResource.put).toHaveBeenCalled();
        expect(StudyService.studySaved).toHaveBeenCalled();
      }));
    });
  });
});
