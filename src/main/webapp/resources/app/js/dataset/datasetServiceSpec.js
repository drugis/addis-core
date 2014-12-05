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

    xdescribe('loadStore', function() {
      it('should load data', inject(function(DatasetService, $rootScope, $q) {

        var data =
          '<http://trials.drugis.org/datasets/ca3fcadd-a227-447f-a473-a7e58fd0ca06> a <http://trials.drugis.org/ontology#Dataset> ;\n' +
          '  <http://www.w3.org/2000/01/rdf-schema#comment> "test"; \n' +
          '  <http://www.w3.org/2000/01/rdf-schema#label> "werkt ie nog";\n' +
          '  <http://purl.org/dc/elements/1.1/creator> "flutadres@gmail.com" .\n';

        var result = DatasetService.loadStore(data);
        // $rootScope.$digest();
        // .then(function(loadResult) {
        //   expect(result).toBe(loadResult);
        // }, function(failureResult) {
        //   expect(false).toBeTrue();
        // });
        // expect(result).not.toBeNull();
        // result.then(function(successResult) {
        //   expect(result).toEqual(4);
        // });
        // $rootScope
        console.log('wut')
        $q.when(result).then(function(){
          console.log("waterpomp tang");
        });

        console.log('digesting');
        window.setInterval(function(){
          $rootScope.$digest();
        }, 5000);

      }));
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
