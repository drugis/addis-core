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

    function getAnalyses(projectId) {
      if (cache.analysesPromises[projectId]) {
        return cache.analysesPromises[projectId];
      }
      cache.analysesPromises[projectId] = AnalysisResource.query(projectId).$promise;
      return cache.analysesPromises[projectId];
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

    function getConsistencyModels(projectId) {
      if (cache.consistecyModelsPromises[projectId]) {
        return cache.consistecyModelsPromises[projectId];
      }
      cache.consistecyModelsPromises[projectId] = ModelResource.getConsistencyModels(projectId).$promise;
      return cache.consistecyModelsPromises[projectId];
    }

    function getModelsByProject(projectId) {
      if (cache.modelsByProjectPromises[projectId]) {
        return cache.modelsByProjectPromises[projectId];
      }
      cache.modelsByProjectPromises[projectId] = ModelResource.queryByProject(projectId).$promise;
      return cache.modelsByProjectPromises[projectId];

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

    function getInterventions(projectId) {
      if (cache.interventionPromises[projectId]) {
        return cache.interventionPromises[projectId];
      }
      cache.interventionPromises[projectId] = InterventionResource.query(projectId).$promise;
      return cache.interventionPromises[projectId];
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
