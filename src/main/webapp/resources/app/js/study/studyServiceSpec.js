'use strict';
define(['../util/context', 'angular-mocks'],
  function(externalContext) {
    describe('study service', function() {

      var studyService,
        uuidServiceMock = jasmine.createSpyObj('UUIDService', ['generate']),
        graphResource = jasmine.createSpyObj('GraphResource', ['putJson']),
        rootScope;

      beforeEach(function() {
        angular.mock.module('trialverse.study');
        angular.mock.module('trialverse.util', function($provide) {
          $provide.value('UUIDService', uuidServiceMock);
        });
        angular.mock.module('trialverse.graph', function($provide) {
          $provide.value('GraphResource', graphResource);
        });
      });

      beforeEach(inject(function($rootScope, StudyService) {
        rootScope = $rootScope;
        studyService = StudyService;
      }));

      describe('createEmptyStudy', function() {
        beforeEach(function() {
          uuidServiceMock.generate.calls.reset();
          uuidServiceMock.generate.and.returnValue('newStudyUid');
        });
        
        it('should return a graph of the new study', function() {
          var newStudy = {
            label: 'label',
            comment: 'comment'
          };
          var result = studyService.createEmptyStudy(newStudy, 'userUid', 'datasetUid');
          var expectNewStudy = {
            '@graph': [{
              '@id': 'http://trials.drugis.org/studies/newStudyUid',
              '@type': 'ontology:Study',
              label: 'label',
              comment: 'comment',
              has_activity: [],
              has_arm: [],
              has_group: [],
              has_included_population: [{
                '@id': 'instance:undefined',
                '@type': 'ontology:StudyPopulation'
              }],
              has_eligibility_criteria: [],
              has_indication: [],
              has_objective: [],
              has_outcome: [],
              has_publication: []
            }],
            '@context': externalContext
          };
          expect(uuidServiceMock.generate.calls.count()).toEqual(1);
          expect(graphResource.putJson).toHaveBeenCalledWith(
            jasmine.any(Object), expectNewStudy, jasmine.any(Function), jasmine.any(Function)
          );
          expect(result.then).toBeDefined();
        });
      });

      describe('reset', function() {
        beforeEach(inject(function($q) {
          var loadDefer = $q.defer();
          studyService.loadJson(loadDefer.promise);
          studyService.save({});
          loadDefer.resolve({
            '@graph': []
          });
          rootScope.$digest();
        }));
        it('should reset the loadDefer and setModified to false', function() {
          expect(studyService.isStudyModified()).toEqual(true);
          studyService.reset();
          expect(studyService.isStudyModified()).toEqual(false);
        });
      });

      describe('isStudyModified', function() {
        beforeEach(inject(function($q) {
          var loadDefer = $q.defer();
          studyService.loadJson(loadDefer.promise);
          studyService.save({});
          loadDefer.resolve({
            '@graph': []
          });
          rootScope.$digest();
        }));
        it('should return true is study is modified', function() {
          expect(studyService.isStudyModified()).toEqual(true);
        });
        it('should return false is study is not modified', function() {
          studyService.reset();
          expect(studyService.isStudyModified()).toEqual(false);
        });
      });

      describe('studySaved', function() {
        beforeEach(inject(function($q) {
          var loadDefer = $q.defer();
          studyService.loadJson(loadDefer.promise);
          studyService.save({});
          loadDefer.resolve({
            '@graph': []
          });
          rootScope.$digest();
        }));
        it('should flip the modified flag', function() {
          expect(studyService.isStudyModified()).toEqual(true);
          studyService.studySaved();
          expect(studyService.isStudyModified()).toEqual(false);
        });
      });

      describe('loadJson', function() {
        var jsonPromise;
        beforeEach(inject(function($q) {
          var defer = $q.defer();
          jsonPromise = defer.promise;
          defer.resolve({
            '@graph': [{
              '@type': 'ontology:Study'
            }]
          });
        }));
        it('should store the json in the service', function(done) {
          studyService.loadJson(jsonPromise);
          // check if we get back what we stored
          studyService.getGraphAndContext().then(function(res) {
            var expectedData = {
              '@graph': [{
                '@type': 'ontology:Study'
              }]
            };
            expect(res).toEqual(expectedData);
            done();
          });
          rootScope.$digest();
        });
      });

      describe('getGraphAndContext', function() {
        var graphPlusContext = {
          '@graph': ['graphItem']
        };
        beforeEach(inject(function($q) {
          var loadDefer = $q.defer();
          studyService.loadJson(loadDefer.promise);
          loadDefer.resolve(graphPlusContext);
          rootScope.$digest();
        }));
        it('should return the whole thing', function(done) {
          studyService.getGraphAndContext().then(function(res) {
            expect(res).toEqual(graphPlusContext);
            done();
          });
          rootScope.$digest();
        });
      });

      describe('getJsonGraph', function() {
        var graphPlusContext = {
          '@graph': ['graphItem']
        };
        beforeEach(inject(function($q) {
          var loadDefer = $q.defer();
          studyService.loadJson(loadDefer.promise);
          loadDefer.resolve(graphPlusContext);
          rootScope.$digest();
        }));
        it('should return only the graph part', function(done) {
          studyService.getJsonGraph().then(function(res) {
            expect(res).toEqual(graphPlusContext['@graph']);
            done();
          });
          rootScope.$digest();
        });
      });

      describe('saveJsonGraph', function() {
        var graphPlusContext = {
          '@graph': ['graphItem']
        };
        beforeEach(inject(function($q) {
          var loadDefer = $q.defer();
          studyService.loadJson(loadDefer.promise);
          loadDefer.resolve(graphPlusContext);
          rootScope.$digest();
        }));
        it('should save the graph and flip the modified flag', function(done) {
          studyService.saveJsonGraph({
            '@graph': ['new graphItem']
          }).then(function() {
            studyService.getJsonGraph().then(function(res) {
              expect(res).toEqual({
                '@graph': ['new graphItem']
              });
              expect(studyService.isStudyModified()).toEqual(true);
              done();
            });
          });
          rootScope.$digest();
        });
      });

      describe('getStudy', function() {
        var graphPlusContext = {
          '@graph': [{
            '@type': 'ontology:Study',
            has_included_population: []
          }]
        };
        beforeEach(inject(function($q) {
          var loadDefer = $q.defer();
          studyService.loadJson(loadDefer.promise);
          loadDefer.resolve(graphPlusContext);
          rootScope.$digest();
        }));
        it('should return just the study part', function(done) {
          studyService.getJsonGraph().then(function(res) {
            expect(res).toEqual([{
              '@type': 'ontology:Study',
              has_included_population: []
            }]);
            done();
          });
          rootScope.$digest();
        });
      });

      describe('save', function() {
        var graphPlusContext = {
          '@graph': [{
            '@type': 'ontology:Study'
          }]
        };
        var copy = angular.copy(graphPlusContext);
        beforeEach(inject(function($q) {
          var loadDefer = $q.defer();
          studyService.loadJson(loadDefer.promise);
          loadDefer.resolve(graphPlusContext);
          rootScope.$digest();
        }));
        it('should store the study in the service', function(done) {
          studyService.save({
            '@type': 'ontology:Study',
            foo: 'bar'
          }).then(function() {
            expect(studyService.isStudyModified()).toEqual(true);
            studyService.getGraphAndContext().then(function(res) {
              expect(res).not.toEqual(copy);
              copy['@graph'][0].foo = 'bar';
              expect(res).toEqual(copy);
              done();
            });
          });
          rootScope.$digest();
        });
      });

    });
  });
