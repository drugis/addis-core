'use strict';
define(['angular-mocks'], function() {
  describe('The caching service,', function() {
    var
      cacheService,
      analysisDefer,
      analysisPromise,
      problemDefer,
      problemPromise,
      modelDefer,
      modelPromise,
      interventionDefer,
      interventionPromise,
      projectId,
      analysisId,
      analysisResourceMock = jasmine.createSpyObj('AnalysisResource', ['get','query']),
      modelResourceMock = jasmine.createSpyObj('ModelResource', ['get', 'getConsistencyModels', 'queryByProject']),
      problemResourceMock = jasmine.createSpyObj('ProblemResource', ['get']),
      interventionResourceMock = jasmine.createSpyObj('InterventionResource',['query']);

    beforeEach(module('addis.project', function($provide) {
      $provide.value('AnalysisResource', analysisResourceMock);
      $provide.value('ProblemResource', problemResourceMock);
      $provide.value('ModelResource', modelResourceMock);
      $provide.value('InterventionResource', interventionResourceMock);
    }));

    beforeEach(inject(function($q) {
      analysisDefer = $q.defer();
      analysisPromise = analysisDefer.promise;
      problemDefer = $q.defer();
      problemPromise = problemDefer.promise;
      modelDefer = $q.defer();
      modelPromise = modelDefer.promise;
      interventionDefer = $q.defer();
      interventionPromise = interventionDefer.promise;
      projectId = 1;
      analysisId = 11;
    }));

    beforeEach(inject(function(CacheService) {
      cacheService = CacheService;
    }));

    it('should not place requests uninvited', function() {
      expect(analysisResourceMock.get).not.toHaveBeenCalled();
      expect(problemResourceMock.get).not.toHaveBeenCalled();
      expect(modelResourceMock.get).not.toHaveBeenCalled();
      expect(modelResourceMock.getConsistencyModels).not.toHaveBeenCalled();
      expect(modelResourceMock.queryByProject).not.toHaveBeenCalled();
      expect(interventionResourceMock.query).not.toHaveBeenCalled();
    });

    describe('getProblem,', function() {
      beforeEach(function() {
        problemResourceMock.get.and.returnValue({
          $promise: problemPromise
        });
      });

      it('for problems not already cached should perform a request and return the resulting promise', function() {
        var resultPromise = cacheService.getProblem(projectId, analysisId);
        expect(resultPromise).toBe(problemPromise);
        expect(problemResourceMock.get).toHaveBeenCalled();
      });

      it('for problems already cached should not perform a request, but should return the resulting promise', function() {
        var resultPromise = cacheService.getProblem(projectId, analysisId);
        expect(resultPromise).toBe(problemPromise);
        problemResourceMock.get.calls.reset();
        resultPromise = cacheService.getProblem(projectId, analysisId);
        expect(resultPromise).toBe(problemPromise);
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
        expect(resultPromise).toBe(analysisPromise);
        expect(analysisResourceMock.get).toHaveBeenCalled();
      });

      it('for analyses already cached should not perform a request, but should return the resulting promise', function() {
        var resultPromise = cacheService.getAnalysis(projectId, analysisId);
        expect(resultPromise).toBe(analysisPromise);
        analysisResourceMock.get.calls.reset();
        resultPromise = cacheService.getAnalysis(projectId, analysisId);
        expect(resultPromise).toBe(analysisPromise);
        expect(analysisResourceMock.get).not.toHaveBeenCalled();
      });

    });

    describe('getAnalyses', function() {
      beforeEach(function() {
        analysisResourceMock.query.and.returnValue({
          $promise: analysisPromise
        });
      });

      it('for analyses not already cached should perform a request and return the resulting promise', function() {
        var resultPromise = cacheService.getAnalyses(projectId);
        expect(resultPromise).toBe(analysisPromise);
        expect(analysisResourceMock.query).toHaveBeenCalled();
      });

      it('for analyses already cached should not perform a request, but should return the resulting promise', function() {
        var resultPromise = cacheService.getAnalyses(projectId);
        expect(resultPromise).toBe(analysisPromise);
        analysisResourceMock.query.calls.reset();
        resultPromise = cacheService.getAnalyses(projectId);
        expect(resultPromise).toBe(analysisPromise);
        expect(analysisResourceMock.query).not.toHaveBeenCalled();
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
        expect(resultPromise).toBe(modelPromise);
        expect(modelResourceMock.get).toHaveBeenCalled();
      });

      it('for models already cached should not perform a request, but should return the resulting promise', function() {
        var modelId = 111;
        var resultPromise = cacheService.getModel(projectId, analysisId, modelId);
        expect(resultPromise).toBe(modelPromise);
        modelResourceMock.get.calls.reset();
        resultPromise = cacheService.getModel(projectId, analysisId, modelId);
        expect(resultPromise).toBe(modelPromise);
        expect(modelResourceMock.get).not.toHaveBeenCalled();
      });
    });

    describe('getConsistencyModels', function() {
      beforeEach(function() {
        modelResourceMock.getConsistencyModels.and.returnValue({
          $promise: modelPromise
        });
      });

      it('for models not already cached should perform a request and return the resulting promise', function() {
        var resultPromise = cacheService.getConsistencyModels(projectId);
        expect(resultPromise).toBe(modelPromise);
        expect(analysisResourceMock.get).not.toHaveBeenCalled();
        expect(problemResourceMock.get).not.toHaveBeenCalled();
        expect(modelResourceMock.getConsistencyModels).toHaveBeenCalled();
      });

      it('for models already cached should not perform a request, but should return the resulting promise', function() {
        var resultPromise = cacheService.getConsistencyModels(projectId);
        expect(resultPromise).toBe(modelPromise);
        modelResourceMock.get.calls.reset();
        resultPromise = cacheService.getConsistencyModels(projectId);
        expect(resultPromise).toBe(modelPromise);
        expect(analysisResourceMock.get).not.toHaveBeenCalled();
        expect(problemResourceMock.get).not.toHaveBeenCalled();
        expect(modelResourceMock.get).not.toHaveBeenCalled();
        expect(modelResourceMock.getConsistencyModels).toHaveBeenCalled();
      });
    });

    describe('getModelsByProject', function() {
      beforeEach(function() {
        modelResourceMock.queryByProject.and.returnValue({
          $promise: modelPromise
        });
      });

      it('for models not already cached should perform a request and return the resulting promise', function() {
        var resultPromise = cacheService.getModelsByProject(projectId);
        expect(resultPromise).toBe(modelPromise);
        expect(modelResourceMock.queryByProject).toHaveBeenCalled();
      });

      it('for models already cached should not perform a request, but should return the resulting promise', function() {
        var resultPromise = cacheService.getModelsByProject(projectId);
        expect(resultPromise).toBe(modelPromise);
        modelResourceMock.queryByProject.calls.reset();
        resultPromise = cacheService.getModelsByProject(projectId);
        expect(resultPromise).toBe(modelPromise);
        expect(modelResourceMock.queryByProject).not.toHaveBeenCalled();
      });
    });

    describe('getInterventions,', function() {
      beforeEach(function() {
        interventionResourceMock.query.and.returnValue({
          $promise: interventionPromise
        });
      });

      it('for models not already cached should perform a request and return the resulting promise', function() {
        var resultPromise = cacheService.getInterventions(projectId);
        expect(resultPromise).toBe(interventionPromise);
        expect(interventionResourceMock.query).toHaveBeenCalled();
      });

      it('for models already cached should not perform a request, but should return the resulting promise', function() {
        var resultPromise = cacheService.getInterventions(projectId);
        expect(resultPromise).toBe(interventionPromise);
        interventionResourceMock.query.calls.reset();
        resultPromise = cacheService.getInterventions(projectId);
        expect(resultPromise).toBe(interventionPromise);
        expect(interventionResourceMock.query).not.toHaveBeenCalled();
      });
    });
  });
});
