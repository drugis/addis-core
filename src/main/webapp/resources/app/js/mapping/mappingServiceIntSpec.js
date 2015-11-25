'use strict';
define(['angular', 'angular-mocks'], function() {
  describe('the mapping service', function() {

    var rootScope, q,
      studyServiceMock = jasmine.createSpyObj('StudyService', ['getJsonGraph', 'saveGraph', 'getStudy', 'saveStudy']),
      mappingService,
      studyDefer,
      studyGraphDefer;

    beforeEach(function() {
      module('trialverse.mapping', function($provide) {
        $provide.value('StudyService', studyServiceMock);
      });
    });

    beforeEach(inject(function($q, $rootScope, MappingService) {
      q = $q;
      rootScope = $rootScope;
      studyGraphDefer = q.defer();
      studyDefer = q.defer();
      studyServiceMock.getJsonGraph.and.returnValue(studyGraphDefer.promise);
      studyServiceMock.getStudy.and.returnValue(studyDefer.promise);

      mappingService = MappingService;
    }));


    fdescribe('set drug mapping where none existed', function() {

      var studyConcept = {
          uri: 'http://testuri/1'
        },
        datasetConcept = {
          uri: 'http://testuri/2',
          type: 'ontology:Drug'
        };
      beforeEach(function(done) {
        studyGraphDefer.resolve([{
          '@id': 'http://testuri/1',
          '@type': 'ontology:Drug',
          'label': 'Sertraline'
        }]);
        mappingService.updateMapping(studyConcept, datasetConcept).then(function() {
          done();
        });
        rootScope.$digest();
      });

      it('should add the new mapping to the graph', function(done) {
        expect(studyServiceMock.saveGraph).toHaveBeenCalledWith([{
          '@id': 'http://testuri/1',
          '@type': 'ontology:Drug',
          'label': 'Sertraline',
          'sameAs': datasetConcept.uri
        }]);
        done();
      });
    });

    fdescribe('set drug mapping where one existed', function() {

      var studyConcept = {
          uri: 'http://testuri/1'
        },
        datasetConcept1 = {
          uri: 'http://testuri/dataset/1',
          type: 'ontology:Drug'
        },
        datasetConcept2 = {
          uri: 'http://testuri/dataset/2',
          type: 'ontology:Drug'
        };

      beforeEach(function(done) {
        studyGraphDefer.resolve([{
          '@id': studyConcept.uri,
          '@type': 'ontology:Drug',
          label: 'Sertraline',
          sameAs: datasetConcept1.uri
        }]);

        mappingService.updateMapping(studyConcept, datasetConcept2).then(function() {
          done();
        });
        rootScope.$digest();
      });

      it('should overwrite the old mapping with the new one', function(done) {
        expect(studyServiceMock.saveGraph).toHaveBeenCalledWith([{
          '@id': studyConcept.uri,
          '@type': 'ontology:Drug',
          label: 'Sertraline',
          sameAs: datasetConcept2.uri
        }]);
        done();
      });
    });

    fdescribe('remove drug mapping', function() {

      var studyConcept = {
          uri: 'http://testuri/1',
          type: 'ontology:Drug'
        },
        datasetConcept = {
          uri: 'http://testuri/dataset/1',
          type: 'ontology:Drug'
        };

      beforeEach(function(done) {
        studyGraphDefer.resolve([{
          '@id': studyConcept.uri,
          '@type': 'ontology:Drug',
          label: 'Sertraline',
          sameAs: datasetConcept.uri
        }]);
        mappingService.removeMapping(studyConcept, datasetConcept).then(function() {
          done();
        });
        rootScope.$digest();
      });

      it('should remove the old mapping', function(done) {
        expect(studyServiceMock.saveGraph).toHaveBeenCalledWith([{
          '@id': studyConcept.uri,
          '@type': 'ontology:Drug',
          label: 'Sertraline'
        }]);
        done();
      });
    });

    fdescribe('set variable mapping where none existed', function() {
      var studyConcept = {
          uri: 'http://testuri/1'
        },
        datasetConcept = {
          uri: 'http://testuri/2',
          type: 'ontology:AdverseEvent'
        };

      beforeEach(function(done) {
        studyDefer.resolve([{
          '@id': 'http://testuri/1',
          '@type': 'ontology:Drug',
          'label': 'Sertraline'
        }]);
        mappingService.updateMapping(studyConcept, datasetConcept).then(function() {
          done();
        });
        rootScope.$digest();
      });

      it('should add the new variable mapping to the graph', function(done) {
        done();
      });
    });

    describe('set variable mapping where one existed', function() {
      var studyConcept = {
          uri: 'http://trials.drugis.org/instances/instance1'
        },
        datasetConcept1 = {
          uri: 'http://trials.drugis.org/entities/entities1',
          type: 'ontology:AdverseEvent'
        },
        datasetConcept2 = {
          uri: 'http://trials.drugis.org/entities/entities2',
          type: 'ontology:AdverseEvent'
        };
      beforeEach(function(done) {
        mappingService.updateMapping(studyConcept, datasetConcept1).then(function() {
          mappingService.updateMapping(studyConcept, datasetConcept2).then(function() {
            done();
          });
        });
        rootScope.$digest();

      });

      it('should overwrite the old variable mapping to the graph', function(done) {
        done();
      });
    });

    describe('remove variable mapping', function() {
      var studyConcept = {
          uri: 'http://trials.drugis.org/instances/instance1'
        },
        datasetConcept = {
          uri: 'http://trials.drugis.org/entities/entities1',
          type: 'ontology:AdverseEvent'
        };

      it('should remove the variable mapping', function(done) {
        done();
      });
    });

  });
});
