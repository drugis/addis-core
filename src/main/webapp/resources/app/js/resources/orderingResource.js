'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var OrderingResource = function($resource) {
    return $resource('/projects/:projectId/analyses/:analysisId/ordering', {
      projectId: '@projectId',
      analysisId: '@analysisId'
    },{
    	put: {
    		method: 'PUT'
    	}
    });
  };
  return dependencies.concat(OrderingResource);
});