'use strict';
define(['angular', 'angular-mocks'],
  function(angular, angularMocks) {
    describe('study service', function() {

      var studyService,
        uuidServiceMock = jasmine.createSpyObj('UUIDService', ['generate']),
        graphResource = jasmine.createSpyObj('GraphResource', ['putJson']),
        rootScope;

      beforeEach(function() {
        module('trialverse.study');
        module('trialverse.util', function($provide) {
          $provide.value('UUIDService', uuidServiceMock);
        });
        module('trialverse.graph', function($provide) {
          $provide.value('GraphResource', graphResource);
        });
      });

      describe('createEmptyStudy', function() {

        beforeEach(angularMocks.inject(function($rootScope, $q, StudyService) {
          rootScope = $rootScope;
          studyService = StudyService;
        }));

        it('should return a graph of the new study', function() {
          var newStudy = {
            label: 'label',
            comment: 'comment'
          };
          var result = studyService.createEmptyStudy(newStudy, 'userUid', 'datasetUid');
          expect(uuidServiceMock.generate).toHaveBeenCalled();
          expect(graphResource.putJson).toHaveBeenCalled();
          expect(result.then).toBeDefined();
        });
      });

      describe('reset', function() {
        beforeEach(angularMocks.inject(function($rootScope, $q, StudyService) {
          rootScope = $rootScope;
          studyService = StudyService;

          var loadDefer = $q.defer();
          var loadPromise = loadDefer.promise;
          studyService.loadJson(loadPromise);
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
        beforeEach(angularMocks.inject(function($rootScope, $q, StudyService) {
          rootScope = $rootScope;
          studyService = StudyService;

          var loadDefer = $q.defer();
          var loadPromise = loadDefer.promise;
          studyService.loadJson(loadPromise);
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
        beforeEach(angularMocks.inject(function($rootScope, $q, StudyService) {
          rootScope = $rootScope;
          studyService = StudyService;

          var loadDefer = $q.defer();
          var loadPromise = loadDefer.promise;
          studyService.loadJson(loadPromise);
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
        beforeEach(angularMocks.inject(function($rootScope, $q, StudyService) {
          rootScope = $rootScope;
          studyService = StudyService;
          var defer = $q.defer();
          jsonPromise = defer.promise;
          defer.resolve('test');
        }));
        it('should store the json in the service', function(done) {
          studyService.loadJson(jsonPromise);
          // check if we get back what we stored
          studyService.getGraphAndContext().then(function(res) {
            expect(res).toEqual('test');
            done();
          });
          rootScope.$digest();
        });
      });

      describe('getGraphAndContext', function() {
        var graphPlusContext = {
          '@graph': ['graphItem']
        };
        beforeEach(angularMocks.inject(function($rootScope, $q, StudyService) {
          studyService = StudyService;
          var loadDefer = $q.defer();
          var loadPromise = loadDefer.promise;
          studyService.loadJson(loadPromise);
          loadDefer.resolve(graphPlusContext);
          rootScope = $rootScope;
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
        beforeEach(angularMocks.inject(function($rootScope, $q, StudyService) {
          studyService = StudyService;
          var loadDefer = $q.defer();
          var loadPromise = loadDefer.promise;
          studyService.loadJson(loadPromise);
          loadDefer.resolve(graphPlusContext);
          rootScope = $rootScope;
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
        beforeEach(angularMocks.inject(function($rootScope, $q, StudyService) {
          studyService = StudyService;
          var loadDefer = $q.defer();
          var loadPromise = loadDefer.promise;
          studyService.loadJson(loadPromise);
          loadDefer.resolve(graphPlusContext);
          rootScope = $rootScope;
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
            '@type': 'ontology:Study'
          }]
        };
        beforeEach(angularMocks.inject(function($rootScope, $q, StudyService) {
          studyService = StudyService;
          var loadDefer = $q.defer();
          var loadPromise = loadDefer.promise;
          studyService.loadJson(loadPromise);
          loadDefer.resolve(graphPlusContext);
          rootScope = $rootScope;
          rootScope.$digest();
        }));
        it('should return just the study part', function(done) {
          studyService.getJsonGraph().then(function(res) {
            expect(res).toEqual([{
              '@type': 'ontology:Study'
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
        beforeEach(angularMocks.inject(function($rootScope, $q, StudyService) {
          studyService = StudyService;
          var loadDefer = $q.defer();
          var loadPromise = loadDefer.promise;
          studyService.loadJson(loadPromise);
          loadDefer.resolve(graphPlusContext);
          rootScope = $rootScope;
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
