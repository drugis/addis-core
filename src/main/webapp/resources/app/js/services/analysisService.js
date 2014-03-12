'use strict';
define(['angular', 'angular-resource'], function (angular, angularResource) {
  var dependencies = ['$resource'];
  var AnalysisService= function ($resource) {
    return $resource('/projects/:projectId/analyses/:analysisId', {projectId: '@projectId', analysisId: '@analysisId'});
  };
  return dependencies.concat(AnalysisService);
});
