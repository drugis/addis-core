
'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('create study controller', function() {

    var scope,
      modalMock = jasmine.createSpyObj('$mock', ['dismiss', 'close']),
      datasetServiceMock = jasmine.createSpyObj('DatasetService', ['addStudyToDatasetGraph']),
      datasetVersionedResourceMock = jasmine.createSpyObj('DatasetVersionedResource', ['save']),
      uuidServiceMock = jasmine.createSpyObj('UUIDService', ['generate']),
      studyServiceMock = jasmine.createSpyObj('StudyService', ['createEmptyStudyJsonLD']),
      graphResourceMock = jasmine.createSpyObj('GraphResource', ['put']),
      mockDatasetsResult,
      mockDatasetsDeferred;

    beforeEach(module('trialverse.dataset'));

    beforeEach(inject(function($rootScope, $q, $controller) {
      scope = $rootScope;
      mockDatasetsDeferred = $q.defer();
      mockDatasetsResult = {
        promise: mockDatasetsDeferred.promise,
      };

      $controller('CreateStudyController', {
        $scope: scope,
        $stateParams: {},
        $modalInstance: modalMock,
        DatasetService: datasetServiceMock,
        DatasetVersionedResource: datasetVersionedResourceMock,
        successCallback: function() {},
        UUIDService: uuidServiceMock,
        StudyService: studyServiceMock,
        GraphResource: graphResourceMock
      });
    }));


    describe('isUniqueShortName', function() {
      it('should be true if the shortname is not already in the list', function() {
        scope.studiesWithDetail = {
          $resolved: true
        };
        expect(scope.isUniqueShortName('connor')).toBe(true);
        scope.studiesWithDetail = [{
          label: 'daan'
        }];
        expect(scope.isUniqueShortName('connor')).toBe(true);
      });
      it('should be false if the shortname is already in the list', function() {
        scope.studiesWithDetail = [{
          label: 'daan'
        }, {
          label: 'connor'
        }];
        expect(scope.isUniqueShortName('connor')).toBe(false);
      });
    });

  });
});
