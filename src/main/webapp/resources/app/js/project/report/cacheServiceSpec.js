'use strict';
define(['angular-mocks', '../project'], function() {
  describe('The caching service,', function() {
    var
      cacheService,
      projectDefer,
      projectPromise,
      analysisDefer,
      analysisPromise,
      problemDefer,
      problemPromise,
      modelDefer,
      modelPromise,
      interventionDefer,
      interventionPromise,
      outcomesDefer,
      outcomesPromise,
      covariatesDefer,
      covariatesPromise,
      projectId,
      analysisId,
      projectResourceMock = jasmine.createSpyObj('projectResource', ['get']),
      analysisResourceMock = jasmine.createSpyObj('AnalysisResource', ['get', 'query']),
      modelResourceMock = jasmine.createSpyObj('ModelResource', ['get', 'getConsistencyModels', 'queryByProject']),
      problemResourceMock = jasmine.createSpyObj('ProblemResource', ['get']),
      interventionResourceMock = jasmine.createSpyObj('InterventionResource', ['query']),
      outcomeResourceMock = jasmine.createSpyObj('OutcomeResource', ['query']),
      covariateResourceMock = jasmine.createSpyObj('CovariateResource', ['query']);

    beforeEach(angular.mock.module('addis.project', function($provide) {
      $provide.value('ProjectResource', projectResourceMock);
      $provide.value('AnalysisResource', analysisResourceMock);
      $provide.value('ProblemResource', problemResourceMock);
      $provide.value('ModelResource', modelResourceMock);
      $provide.value('InterventionResource', interventionResourceMock);
      $provide.value('OutcomeResource', outcomeResourceMock);
      $provide.value('CovariateResource', covariateResourceMock);
    }));

    beforeEach(inject(function($q) {
      projectDefer = $q.defer();
      projectPromise = projectDefer.promise;
      analysisDefer = $q.defer();
      analysisPromise = analysisDefer.promise;
      problemDefer = $q.defer();
      problemPromise = problemDefer.promise;
      modelDefer = $q.defer();
      modelPromise = modelDefer.promise;
      interventionDefer = $q.defer();
      interventionPromise = interventionDefer.promise;
      outcomesDefer = $q.defer();
      outcomesPromise = outcomesDefer.promise;
      covariatesDefer = $q.defer();
      covariatesPromise = covariatesDefer.promise;
      projectId = 1;
      analysisId = 11;
    }));

    beforeEach(inject(function(CacheService) {
      cacheService = CacheService;
    }));

    afterEach(function() {
      projectResourceMock.get.calls.reset();
      analysisResourceMock.get.calls.reset();
      problemResourceMock.get.calls.reset();
      modelResourceMock.get.calls.reset();
      modelResourceMock.getConsistencyModels.calls.reset();
      modelResourceMock.queryByProject.calls.reset();
      interventionResourceMock.query.calls.reset();
      outcomeResourceMock.query.calls.reset();
      covariateResourceMock.query.calls.reset();
    });

    it('should not place requests uninvited', function() {
      expect(projectResourceMock.get).not.toHaveBeenCalled();
      expect(analysisResourceMock.get).not.toHaveBeenCalled();
      expect(problemResourceMock.get).not.toHaveBeenCalled();
      expect(modelResourceMock.get).not.toHaveBeenCalled();
      expect(modelResourceMock.getConsistencyModels).not.toHaveBeenCalled();
      expect(modelResourceMock.queryByProject).not.toHaveBeenCalled();
      expect(interventionResourceMock.query).not.toHaveBeenCalled();
      expect(outcomeResourceMock.query).not.toHaveBeenCalled();
      expect(covariateResourceMock.query).not.toHaveBeenCalled();
    });

    describe('getProject,', function() {
      var params = {
        projectId: projectId
      };
      beforeEach(function() {
        projectResourceMock.get.and.returnValue({
          $promise: projectPromise
        });
      });

      it('for projects not already cached should perform a request and return the resulting promise', function() {
        var resultPromise = cacheService.getProject(params);
        expect(resultPromise).toBe(projectPromise);
        expect(projectResourceMock.get).toHaveBeenCalled();
      });

      it('for projects already cached should not perform a request, but should return the resulting promise', function() {
        var resultPromise = cacheService.getProject(params);
        expect(resultPromise).toBe(projectPromise);
        projectResourceMock.get.calls.reset();
        resultPromise = cacheService.getProject(params);
        expect(resultPromise).toBe(projectPromise);
        expect(projectResourceMock.get).not.toHaveBeenCalled();
      });
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
      var modelId = 111;
      beforeEach(function() {
        modelResourceMock.get.and.returnValue({
          $promise: modelPromise
        });
      });

      it('for models not already cached should perform a request and return the resulting promise', function() {
        var resultPromise = cacheService.getModel(projectId, analysisId, modelId);
        expect(resultPromise).toBe(modelPromise);
        expect(modelResourceMock.get).toHaveBeenCalled();
      });

      it('for models already cached should not perform a request, but should return the resulting promise', function() {
        var resultPromise = cacheService.getModel(projectId, analysisId, modelId);
        expect(resultPromise).toBe(modelPromise);
        modelResourceMock.get.calls.reset();
        resultPromise = cacheService.getModel(projectId, analysisId, modelId);
        expect(resultPromise).toBe(modelPromise);
        expect(modelResourceMock.get).not.toHaveBeenCalled();
      });
    });

    describe('getConsistencyModels', function() {
      var params = {
        projectId: projectId,
        analysisId: analysisId
      };

      beforeEach(function() {
        modelResourceMock.getConsistencyModels.and.returnValue({
          $promise: modelPromise
        });
      });

      it('for models not already cached should perform a request and return the resulting promise', function() {
        var resultPromise = cacheService.getConsistencyModels(params);
        expect(resultPromise).toBe(modelPromise);
        expect(analysisResourceMock.get).not.toHaveBeenCalled();
        expect(problemResourceMock.get).not.toHaveBeenCalled();
        expect(modelResourceMock.getConsistencyModels).toHaveBeenCalled();
      });

      it('for models already cached should not perform a request, but should return the resulting promise', function() {
        var resultPromise = cacheService.getConsistencyModels(params);
        expect(resultPromise).toBe(modelPromise);
        modelResourceMock.get.calls.reset();
        resultPromise = cacheService.getConsistencyModels(params);
        expect(resultPromise).toBe(modelPromise);
        expect(analysisResourceMock.get).not.toHaveBeenCalled();
        expect(problemResourceMock.get).not.toHaveBeenCalled();
        expect(modelResourceMock.get).not.toHaveBeenCalled();
        expect(modelResourceMock.getConsistencyModels).toHaveBeenCalled();
      });
    });

    describe('getModelsByProject', function() {
      var params = {
        projectId: projectId
      };
      beforeEach(function() {
        modelResourceMock.queryByProject.and.returnValue({
          $promise: modelPromise
        });
      });

      it('for models not already cached should perform a request and return the resulting promise', function() {
        var resultPromise = cacheService.getModelsByProject(params);
        expect(resultPromise).toBe(modelPromise);
        expect(modelResourceMock.queryByProject).toHaveBeenCalled();
      });

      it('for models already cached should not perform a request, but should return the resulting promise', function() {
        var resultPromise = cacheService.getModelsByProject(params);
        expect(resultPromise).toBe(modelPromise);
        modelResourceMock.queryByProject.calls.reset();
        resultPromise = cacheService.getModelsByProject(params);
        expect(resultPromise).toBe(modelPromise);
        expect(modelResourceMock.queryByProject).not.toHaveBeenCalled();
      });
    });

    describe('getInterventions,', function() {
      var params = {
        projectId: projectId
      };
      beforeEach(function() {
        interventionResourceMock.query.and.returnValue({
          $promise: interventionPromise
        });
      });

      it('for interventions not already cached should perform a request and return the resulting promise', function() {
        var resultPromise = cacheService.getInterventions(params);
        expect(resultPromise).toBe(interventionPromise);
        expect(interventionResourceMock.query).toHaveBeenCalled();
      });

      it('for interventions already cached should not perform a request, but should return the resulting promise', function() {
        var resultPromise = cacheService.getInterventions(params);
        expect(resultPromise).toBe(interventionPromise);
        interventionResourceMock.query.calls.reset();
        resultPromise = cacheService.getInterventions(params);
        expect(resultPromise).toBe(interventionPromise);
        expect(interventionResourceMock.query).not.toHaveBeenCalled();
      });
    });
    describe('getOutcomes,', function() {
      var params = {
        projectId: projectId
      };

      beforeEach(function() {
        outcomeResourceMock.query.and.returnValue({
          $promise: outcomesPromise
        });
      });

      it('for outcomes not already cached should perform a request and return the resulting promise', function() {
        var resultPromise = cacheService.getOutcomes(params);
        expect(resultPromise).toBe(outcomesPromise);
        expect(outcomeResourceMock.query).toHaveBeenCalled();
      });

      it('for outcomes already cached should not perform a request, but should return the resulting promise', function() {
        var resultPromise = cacheService.getOutcomes(params);
        expect(resultPromise).toBe(outcomesPromise);
        outcomeResourceMock.query.calls.reset();
        resultPromise = cacheService.getOutcomes(params);
        expect(resultPromise).toBe(outcomesPromise);
        expect(outcomeResourceMock.query).not.toHaveBeenCalled();
      });
    });
    describe('getCovariates,', function() {
      var params = {
        projectId: projectId
      };
      beforeEach(function() {
        covariateResourceMock.query.and.returnValue({
          $promise: covariatesPromise
        });
      });

      it('for covariates not already cached should perform a request and return the resulting promise', function() {
        var resultPromise = cacheService.getCovariates(params);
        expect(resultPromise).toBe(covariatesPromise);
        expect(covariateResourceMock.query).toHaveBeenCalled();
      });

      it('for covariates already cached should not perform a request, but should return the resulting promise', function() {
        var resultPromise = cacheService.getCovariates(params);
        expect(resultPromise).toBe(covariatesPromise);
        covariateResourceMock.query.calls.reset();
        resultPromise = cacheService.getCovariates(params);
        expect(resultPromise).toBe(covariatesPromise);
        expect(covariateResourceMock.query).not.toHaveBeenCalled();
      });
    });
  });
});
