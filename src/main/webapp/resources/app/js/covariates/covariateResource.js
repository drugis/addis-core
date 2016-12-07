'use strict';
define(['angular', 'angular-resource'], function() {
  var dependencies = ['$resource'];
  var CovariateResource = function($resource) {
    return $resource('/projects/:projectId/covariates/:covariateId', {
      projectId: '@projectId',
      covariateId: '@id'
    }, {
      'delete': {
        method: 'DELETE'
      }
    });
  };
  return dependencies.concat(CovariateResource);
});
