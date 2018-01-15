'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var ToggleColumnsResource = function($resource) {
    return $resource('/projects/:projectId/analyses/:analysisId/toggledColumns', {
      projectId: '@projectId',
      analysisId: '@analysisId'
    },{
    	put: {
    		method: 'PUT'
    	}
    });
  };
  return dependencies.concat(ToggleColumnsResource);
});