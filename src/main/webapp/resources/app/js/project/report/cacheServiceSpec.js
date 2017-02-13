'use strict';
define(['angular-mocks'], function() {
  fdescribe('The caching service,', function() {
    var
      cacheService,
      analysisDefer,
      analysisPromise,
      problemDefer,
      problemPromise,
      modelDefer,
      modelPromise,
      projectId,
      analysisId,
      analysisResourceMock = jasmine.createSpyObj('AnalysisResource', ['get']),
      modelResourceMock = jasmine.createSpyObj('ModelResource', ['get']),
      problemResourceMock = jasmine.createSpyObj('ProblemResource', ['get']);

    beforeEach(module('addis.project', function($provide) {
      $provide.value('AnalysisResource', analysisResourceMock);
      $provide.value('ProblemResource', problemResourceMock);
      $provide.value('ModelResource', modelResourceMock);
    }));

    beforeEach(inject(function($q) {
      analysisDefer = $q.defer();
      analysisPromise = analysisDefer.promise;
      problemDefer = $q.defer();
      problemPromise = problemDefer.promise;
      modelDefer = $q.defer();
      modelPromise = modelDefer.promise;
      projectId = 1;
      analysisId = 11;
    }));

    beforeEach(inject(function(CacheService) {
      cacheService = CacheService;
    }));

    it('should not place requests uninvited', function() {
      expect(analysisResourceMock.get).not.toHaveBeenCalled();
      expect(problemResourceMock.get).not.toHaveBeenCalled();
    });

    describe('getProblem,', function() {
      beforeEach(function() {
        problemResourceMock.get.and.returnValue({
          $promise: problemPromise
        });
      });

      it('for problems not already cached should perform a request and return the resulting promise', function() {
        var resultPromise = cacheService.getProblem(projectId, analysisId);
        expect(resultPromise).toEqual(problemPromise);
        expect(problemResourceMock.get).toHaveBeenCalled();
        expect(analysisResourceMock.get).not.toHaveBeenCalled();
      });

      it('for problems already cached should not perform a request, but should return the resulting promise', function() {
        var resultPromise = cacheService.getProblem(projectId, analysisId);
        expect(resultPromise).toEqual(problemPromise);
        problemResourceMock.get.calls.reset();
        resultPromise = cacheService.getProblem(projectId, analysisId);
        expect(resultPromise).toEqual(problemPromise);
        expect(analysisResourceMock.get).not.toHaveBeenCalled();
        expect(problemResourceMock.get).not.toHaveBeenCalled();
      });
    });
    describe('getAnalysis,', function() {
      beforeEach(function() {
        analysisResourceMock.get.and.returnValue({
          $promise: analysisPromise
        });
      });

      it('for analyses not already cached should perform a request and return the resulting promise', function() {
        var resultPromise = cacheService.getAnalysis(projectId, analysisId);
        expect(resultPromise).toEqual(analysisPromise);
        expect(analysisResourceMock.get).toHaveBeenCalled();
        expect(problemResourceMock.get).not.toHaveBeenCalled();
      });

      it('for analyses already cached should not perform a request, but should return the resulting promise', function() {
        var resultPromise = cacheService.getAnalysis(projectId, analysisId);
        expect(resultPromise).toEqual(analysisPromise);
        analysisResourceMock.get.calls.reset();
        resultPromise = cacheService.getAnalysis(projectId, analysisId);
        expect(resultPromise).toEqual(analysisPromise);
        expect(analysisResourceMock.get).not.toHaveBeenCalled();
        expect(problemResourceMock.get).not.toHaveBeenCalled();
      });

    });
    describe('getModel,', function() {
      beforeEach(function() {
        modelResourceMock.get.and.returnValue({
          $promise: modelPromise
        });
      });

      it('for models not already cached should perform a request and return the resulting promise', function() {
        var modelId = 111;
        var resultPromise = cacheService.getModel(projectId, analysisId, modelId);
        expect(resultPromise).toEqual(modelPromise);
        expect(analysisResourceMock.get).not.toHaveBeenCalled();
        expect(problemResourceMock.get).not.toHaveBeenCalled();
        expect(modelResourceMock.get).toHaveBeenCalled();
      });

      it('for analyses already cached should not perform a request, but should return the resulting promise', function() {
        var modelId = 111;
        var resultPromise = cacheService.getModel(projectId, analysisId, modelId);
        expect(resultPromise).toEqual(modelPromise);
        analysisResourceMock.get.calls.reset();
        resultPromise = cacheService.getModel(projectId, analysisId, modelId);
        expect(resultPromise).toEqual(modelPromise);
        expect(analysisResourceMock.get).not.toHaveBeenCalled();
        expect(problemResourceMock.get).not.toHaveBeenCalled();
        expect(modelResourceMock.get).not.toHaveBeenCalled();
      });
    });

    describe('getConsistencyModels', function() {
      beforeEach(function() {
        modelResourceMock.get.and.returnValue({
          $promise: modelPromise
        });
      });

      it('for models not already cached should perform a request and return the resulting promise', function() {
        var resultPromise = cacheService.getModel(projectId, analysisId);
        expect(resultPromise).toEqual(modelPromise);
        expect(analysisResourceMock.get).not.toHaveBeenCalled();
        expect(problemResourceMock.get).not.toHaveBeenCalled();
        expect(modelResourceMock.get).toHaveBeenCalled();
      });

      it('for models already cached should not perform a request, but should return the resulting promise', function() {
        var resultPromise = cacheService.getModel(projectId, analysisId);
        expect(resultPromise).toEqual(modelPromise);
        analysisResourceMock.get.calls.reset();
        resultPromise = cacheService.getModel(projectId, analysisId);
        expect(resultPromise).toEqual(modelPromise);
        expect(analysisResourceMock.get).not.toHaveBeenCalled();
        expect(problemResourceMock.get).not.toHaveBeenCalled();
        expect(modelResourceMock.get).not.toHaveBeenCalled();
      });
    });

  });
});
