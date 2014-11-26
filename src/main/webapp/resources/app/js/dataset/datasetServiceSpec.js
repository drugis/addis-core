'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('dataset service', function() {

    var mockDatasetResource,
      mockRdfstoreService,
      mockStore,
      mockDatasets = [{
        datasetUri: {
          value: 'a/uid1'
        }
      }, {
        datasetUri: {
          value: 'a/uid2'
        }
      }];

    beforeEach(module('trialverse.dataset'));

    beforeEach(function() {
      mockDatasetResource = jasmine.createSpyObj('DatasetResource', ['query']);
      mockRdfstoreService = jasmine.createSpyObj('RdfstoreService', ['load', 'execute']);

      module('trialverse', function($provide) {
        $provide.value('DatasetResource', mockDatasetResource);
        $provide.value('RdfstoreService', mockRdfstoreService);
      });
    });

    describe('getDatasets', function() {
      var resourceDeferred, storeDeferred, datasetsDeferred, result;

      beforeEach(inject(function($q, DatasetService) {
        resourceDeferred = $q.defer();
        var resourceResult = {
          $promise: resourceDeferred.promise
        };
        storeDeferred = $q.defer();
        datasetsDeferred = $q.defer();
        mockDatasetResource.query.and.returnValue(resourceResult);
        mockRdfstoreService.load.and.returnValue(storeDeferred);
        mockRdfstoreService.execute.and.returnValue(datasetsDeferred);
        result = DatasetService.getDatasets();
      }));

      it('should query the datasetResource', function() {
        expect(mockDatasetResource.query).toHaveBeenCalled();
        expect(result.promise).not.toBe(null);
      });

      describe('when all promises are resolved', function() {

        it('should resolve to a list of datasets', inject(function($rootScope) {
          resourceDeferred.resolve(resourceDeferred);
          storeDeferred.resolve(mockStore);
          datasetsDeferred.resolve(mockDatasets);

          var resolvedResult;
          result.promise.then(function(result) {
            resolvedResult = result;
          });

          $rootScope.$digest();

          expect(resolvedResult.length).toBe(2);
          expect(resolvedResult[0].uuid).toBe('uid1');
          expect(resolvedResult).not.toBe(undefined);
        }));

      });

    });

    describe('addStudyToDatasetGraph', function() {

      describe('when the graph does not contain any studies', function() {

        it('should add context, and a single contains_study', inject(function(DatasetService) {
          var emptyDatsetGraph = {
            '@context' : {}
          };
          var newUUID = 'newuuid';
          var result = DatasetService.addStudyToDatasetGraph(newUUID, emptyDatsetGraph);
          expect(result['@context'].contains_study).toBeDefined();
          expect(result.contains_study).toEqual('study:' + newUUID);
        }));
      });


      describe('when the graph contains precisely one study', function() {

        it('should convert contains_study to an array and add the new study', inject(function(DatasetService) {
          var emptyDatsetGraph = {
            '@context' : {},
            contains_study : 'pre-existing-uuid'
          };
          var newUUID = 'newuuid';
          var result = DatasetService.addStudyToDatasetGraph(newUUID, emptyDatsetGraph);
          expect(result['@context'].contains_study).toBeDefined();
          expect(result.contains_study).toEqual(['study:' + newUUID, 'pre-existing-uuid']);
        }));
      });
      describe('when the graph contains more than one study', function() {

        it('should add the new study', inject(function(DatasetService) {
          var preExistingUuids  = ['pre-existing-uuid1', 'pre-existing-uuid2'], 
          emptyDatsetGraph = {
            '@context' : {},
            contains_study : preExistingUuids
          };
          var newUUID = 'newuuid';
          var result = DatasetService.addStudyToDatasetGraph(newUUID, emptyDatsetGraph);
          expect(result['@context'].contains_study).toBeDefined();
          expect(result.contains_study).toEqual(['study:' + newUUID].concat(preExistingUuids));
        }));
      });

    });

  });
});
