'use strict';

define(['lodash'], function(_) {
  var dependencies = ['$q', 'AnalysisResource', 'ModelResource', 'ProblemResource', 'InterventionResource'];

  var CacheService = function($q, AnalysisResource, ModelResource, ProblemResource, InterventionResource) {
    var cache = {
      analysesPromises: {},
      modelPromises: [],
      problemPromises: {},
      consistecyModelsPromises: {},
      modelsByProjectPromises: {},
      interventionPromises: {}
    };

    function getAnalysis(projectId, analysisId) {
      if (cache.analysesPromises[analysisId]) {
        return cache.analysesPromises[analysisId];
      }
      cache.analysesPromises[analysisId] = AnalysisResource.get({
        projectId: projectId,
        analysisId: analysisId
      }).$promise;
      return cache.analysesPromises[analysisId];
    }

    function getAnalyses(params) {
      if (cache.analysesPromises[params.projectId]) {
        return cache.analysesPromises[params.projectId];
      }
      cache.analysesPromises[params.projectId] = AnalysisResource.query(params).$promise;
      return cache.analysesPromises[params.projectId];
    }


    function getModel(projectId, analysisId, modelId) {
      var modelObject = _.find(cache.modelPromises, function(modelPromise) {
        return modelPromise.analysisId === analysisId && modelPromise.modelId === modelId;
      });
      if (modelObject) {
        return modelObject.promise;
      }
      modelObject = {
        analysisId: analysisId,
        modelId: modelId,
        promise: ModelResource.get({
          projectId: projectId,
          analysisId: analysisId,
          modelId: modelId
        }).$promise
      };
      cache.modelPromises.push(modelObject);

      return modelObject.promise;
    }

    function getConsistencyModels(params) {
      if (cache.consistecyModelsPromises[params.projectId]) {
        return cache.consistecyModelsPromises[params.projectId];
      }
      cache.consistecyModelsPromises[params.projectId] = ModelResource.getConsistencyModels(params).$promise;
      return cache.consistecyModelsPromises[params.projectId];
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

    return {
      getAnalysis: getAnalysis,
      getAnalyses: getAnalyses,
      getModel: getModel,
      getProblem: getProblem,
      getConsistencyModels: getConsistencyModels,
      getModelsByProject: getModelsByProject,
      getInterventions: getInterventions
    };
  };
  return dependencies.concat(CacheService);
});
