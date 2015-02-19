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
      mockDatasetService = jasmine.createSpyObj('DatasetService', ['reset', 'loadStore', 'queryDataset']),
      resultsServiceMock = jasmine.createSpyObj('ResultsService', ['cleanUpMeasurements']),
      loadStoreDeferred,
      loadDatasetStoreDeferred,
      queryStudyDataDeferred,
      queryArmDataDeferred,
      getStudyGraphDeferred;


    beforeEach(module('trialverse.study'));

    beforeEach(inject(function($rootScope, $q, $controller, $httpBackend, StudyResource, DatasetResource ) {

      scope = $rootScope;
      httpBackend = $httpBackend;

      httpBackend.expectGET('/datasets/' + datasetUUID + '/studies/' + studyUUID).respond('study');
      httpBackend.expectGET('/datasets/datasetUUID?studyUUID=studyUUID').respond('dataset');



      loadStoreDeferred = $q.defer();
      queryStudyDataDeferred = $q.defer();
      queryArmDataDeferred = $q.defer();
      getStudyGraphDeferred = $q.defer();
      loadDatasetStoreDeferred = $q.defer();

      mockStudyService.loadStore.and.returnValue(loadStoreDeferred.promise);
      mockStudyService.queryStudyData.and.returnValue(queryStudyDataDeferred.promise);
      mockStudyService.queryArmData.and.returnValue(queryArmDataDeferred.promise);
      mockStudyService.getStudyGraph.and.returnValue(getStudyGraphDeferred.promise);

      mockDatasetService.loadStore.and.returnValue(loadDatasetStoreDeferred.promise);

      $controller('StudyController', {
        $scope: scope,
        $stateParams: mockStateParams,
        StudyResource: StudyResource,
        $location: mockLocation,
        $anchorScroll: mockAnchorScroll,
        $modal: mockModal,
        $window: {bind: 'mockBind'},
        StudyService: mockStudyService,
        DatasetResource: DatasetResource,
        DatasetService: mockDatasetService,
        ResultsService: resultsServiceMock
      });
    }));

    describe('on load', function() {

      it('should place study on the scope', function() {
        var
          studyQueryResult = 'studyQueryResult',
          armQueryResult = 'armQueryResult';

        expect(scope.study).toBeDefined();
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
      it('should export the graph and PUT it to the resource', inject(function(StudyResource, $q) {

        httpBackend.expectPUT('/datasets/datasetUUID/studies/studyUUID').respond(200,'');

        scope.saveStudy();
        expect(mockStudyService.getStudyGraph).toHaveBeenCalled();

        getStudyGraphDeferred.resolve({data: 'mock study data'});
        scope.$digest();
        httpBackend.flush();

        expect(mockStudyService.studySaved).toHaveBeenCalled();
      }));
    });
  });
});
