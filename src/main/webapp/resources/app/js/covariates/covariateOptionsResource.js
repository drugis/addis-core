'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var CovariateOptionsResource = function($resource) {
    return $resource('/covariate-options', {}, {
      getProjectCovariates: {
        method: 'GET',
        url: '/projects/:projectId/covariate-options',
        params: {
          projectId: '@projecId'
        },
        isArray: true
      }
    });
  };
  return dependencies.concat(CovariateOptionsResource);
});
