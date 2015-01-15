'use strict';
define([],
  function() {
    var dependencies = ['$q'];
    var MeasurementMomentService = function($q) {
      function queryItems() {
        return $q.defer().promise;
      }

      return { queryItems: queryItems};
    };
    return dependencies.concat(MeasurementMomentService);
  });
