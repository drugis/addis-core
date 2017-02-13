'use strict';

define(['lodash'], function(_) {
  var dependencies = ['$q', 'AnalysisResource', 'ModelResource', 'ProblemResource'];

  var CacheService = function($q, AnalysisResource, ModelResource, ProblemResource) {
    var cache = {
      analysesPromises: {},
      modelPromises: {},
      problemPromises: []
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
      cache.problemPromises.push(modelObject);

      return modelObject.promise;
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

    return {
      getAnalysis: getAnalysis,
      getModel: getModel,
      getProblem: getProblem
    };
  };
  return dependencies.concat(CacheService);
});
