'use strict';

define([], function() {
  var dependencies = ['$q', 'AnalysisResource', 'ModelResource', 'ProblemResource', 'InterventionResource'];

  var CacheService = function($q, AnalysisResource, ModelResource, ProblemResource, InterventionResource) {
    var cache = {
      analysesPromises: {},
      analysisPromises: {},
      modelPromises: {},
      problemPromises: {},
      consistencyModelsPromises: {},
      modelsByProjectPromises: {},
      interventionPromises: {}
    };

    function getAnalysis(projectId, analysisId) {
      if (cache.analysisPromises[analysisId]) {
        return cache.analysisPromises[analysisId];
      }
      cache.analysisPromises[analysisId] = AnalysisResource.get({
        projectId: projectId,
        analysisId: analysisId
      }).$promise;
      return cache.analysisPromises[analysisId];
    }

    function getAnalyses(params) {
      if (cache.analysesPromises[params.projectId]) {
        return cache.analysesPromises[params.projectId];
      }
      cache.analysesPromises[params.projectId] = AnalysisResource.query(params).$promise;
      return cache.analysesPromises[params.projectId];
    }


    function getModel(projectId, analysisId, modelId) {
      if (cache.modelPromises[modelId]) {
        return cache.modelPromises[modelId];
      }
      cache.modelPromises[modelId] = ModelResource.get({
        projectId: projectId,
        analysisId: analysisId,
        modelId: modelId
      }).$promise;
      return cache.modelPromises[modelId];
    }

    function getConsistencyModels(params) {
      if (cache.consistencyModelsPromises[params.projectId]) {
        return cache.consistencyModelsPromises[params.projectId];
      }
      cache.consistencyModelsPromises[params.projectId] = ModelResource.getConsistencyModels(params).$promise;
      return cache.consistencyModelsPromises[params.projectId];
    }

    function getModelsByProject(params) {
      if (cache.modelsByProjectPromises[params.projectId]) {
        return cache.modelsByProjectPromises[params.projectId];
      }
      cache.modelsByProjectPromises[params.projectId] = ModelResource.queryByProject(params).$promise;
      return cache.modelsByProjectPromises[params.projectId];

    }

    function getProblem(projectId, analysisId) {
      if (cache.problemPromises[analysisId]) {
        return cache.problemPromises[analysisId];
      }
      cache.problemPromises[analysisId] = ProblemResource.get({
        projectId: projectId,
        analysisId: analysisId
      }).$promise;
      return cache.problemPromises[analysisId];
    }

    function getInterventions(params) {
      if (cache.interventionPromises[params.projectId]) {
        return cache.interventionPromises[params.projectId];
      }
      cache.interventionPromises[params.projectId] = InterventionResource.query(params).$promise;
      return cache.interventionPromises[params.projectId];
    }

    function evict(cacheName, key) {
        delete cache[cacheName][key];
    }

    return {
      getAnalysis: getAnalysis,
      getAnalyses: getAnalyses,
      getModel: getModel,
      getProblem: getProblem,
      getConsistencyModels: getConsistencyModels,
      getModelsByProject: getModelsByProject,
      getInterventions: getInterventions,
      evict: evict
    };
  };
  return dependencies.concat(CacheService);
});
