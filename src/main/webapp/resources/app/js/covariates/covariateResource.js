'use strict';
define(['angular', 'angular-resource'], function(angular, angularResource) {
  var dependencies = ['$resource'];
  var CovariateResource = function($resource) {
    return $resource('/projects/:projectId/covariates/:covariateId', {
      projectId: '@projectId',
      covariateId: '@id'
    });
  };
  return dependencies.concat(CovariateResource);
});
