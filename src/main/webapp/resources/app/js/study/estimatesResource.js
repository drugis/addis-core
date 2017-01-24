'use strict';
define([], function() {
  var dependencies = ['$resource'];
  var EstimatesResource = function($resource) {
    return $resource('/statistics/', {}, {
      getEstimates: {
        url: '/statistics/estimates',
        method: 'POST'
      }
    });
  };
  return dependencies.concat(EstimatesResource);
});
