'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('dataset service', function() {

    beforeEach(module('trialverse.dataset'));

    describe('loadStore', function() {

      beforeEach(inject(function($rootScope, $q, StudyService) {
        studyService = StudyService;
        q = $q;
        scope = $rootScope;
      }));

      beforeEach(, function(done) {
        setTimeout(function() {
          value = 0;
          done();
        }, 1);
      });

      it('should query the datasetResource', inject(function($q, DatasetService) {
        var data = '<http://trials.drugis.org/datasets/ca3fcadd-a227-447f-a473-a7e58fd0ca06> a <http://trials.drugis.org/ontology#Dataset> ;\n' +
          '  <http://www.w3.org/2000/01/rdf-schema#comment> "test"; \n' +
          '  <http://www.w3.org/2000/01/rdf-schema#label> "werkt ie nog";\n' +
          '  <http://purl.org/dc/elements/1.1/creator> "flutadres@gmail.com" .\n';


        var promise = DatasetService.loadStore(data);

        expect(promise.$$state.value).toBeDefined();
      }));

      xdescribe('when all promises are resolved', function() {

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

    xdescribe('addStudyToDatasetGraph', function() {

      describe('when the graph does not contain any studies', function() {

        it('should add context, and a single contains_study', inject(function(DatasetService) {
          var emptyDatsetGraph = {
            '@context': {}
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
            '@context': {},
            contains_study: 'pre-existing-uuid'
          };
          var newUUID = 'newuuid';
          var result = DatasetService.addStudyToDatasetGraph(newUUID, emptyDatsetGraph);
          expect(result['@context'].contains_study).toBeDefined();
          expect(result.contains_study).toEqual(['study:' + newUUID, 'pre-existing-uuid']);
        }));
      });
      describe('when the graph contains more than one study', function() {

        it('should add the new study', inject(function(DatasetService) {
          var preExistingUuids = ['pre-existing-uuid1', 'pre-existing-uuid2'],
            emptyDatsetGraph = {
              '@context': {},
              contains_study: preExistingUuids
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