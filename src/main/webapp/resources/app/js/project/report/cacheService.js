'use strict';

define([], function() {
  var dependencies = ['$q', 'ProjectResource', 'AnalysisResource', 'ModelResource', 'ProblemResource',
    'InterventionResource', 'OutcomeResource', 'CovariateResource'
  ];

  var CacheService = function($q, ProjectResource, AnalysisResource, ModelResource, ProblemResource,
    InterventionResource, OutcomeResource, CovariateResource) {
    var cache = {
      projectPromises: {},
      analysesPromises: {},
      analysisPromises: {},
      modelPromises: {},
      problemPromises: {},
      consistencyModelsPromises: {},
      modelsByProjectPromises: {},
      interventionsPromises: {},
      outcomesPromises: {},
      covariatesPromises: {}
    };

    function getProject(params) {
      if(cache.projectPromises[params.projectId]) {
        return cache.projectPromises[params.projectId];
      }
      cache.projectPromises[params.projectId] = ProjectResource.get(params).$promise;
      return cache.projectPromises[params.projectId];
    }

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
      if (cache.interventionsPromises[params.projectId]) {
        return cache.interventionsPromises[params.projectId];
      }
      cache.interventionsPromises[params.projectId] = InterventionResource.query(params).$promise;
      return cache.interventionsPromises[params.projectId];
    }

    function getOutcomes(params) {
      if (cache.outcomesPromises[params.projectId]) {
        return cache.outcomesPromises[params.projectId];
      }
      cache.outcomesPromises[params.projectId] = OutcomeResource.query(params).$promise;
      return cache.outcomesPromises[params.projectId];
    }

    function getCovariates(params) {
      if (cache.covariatesPromises[params.projectId]) {
        return cache.covariatesPromises[params.projectId];
      }
      cache.covariatesPromises[params.projectId] = CovariateResource.query(params).$promise;
      return cache.covariatesPromises[params.projectId];
    }

    function evict(cacheName, key) {
      delete cache[cacheName][key];
    }

    return {
      getProject: getProject,
      getAnalysis: getAnalysis,
      getAnalyses: getAnalyses,
      getModel: getModel,
      getProblem: getProblem,
      getConsistencyModels: getConsistencyModels,
      getModelsByProject: getModelsByProject,
      getInterventions: getInterventions,
      getOutcomes: getOutcomes,
      getCovariates: getCovariates,
      evict: evict
    };
  };
  return dependencies.concat(CacheService);
});
