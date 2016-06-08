'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var RepairService = function() {

    function findOverlappingResults(sourceResults, targetResults, isOverlappingFunction) {
      return _.reduce(sourceResults, function(accum, sourceResult) {
        var targetResult = _.find(targetResults, function(targetResult) {
          return isOverlappingFunction(sourceResult, targetResult);
        });
        if (targetResult) {
          accum.push(sourceResult);
        }
        return accum;
      }, []);
    }

    function findNonOverlappingResults(sourceResults, targetResults, isOverlappingFunction) {
      return _.reduce(sourceResults, function(accum, sourceResult) {
        var targetResult = _.find(targetResults, function(targetResult) {
          return isOverlappingFunction(sourceResult, targetResult);
        });
        if (!targetResult) {
          accum.push(sourceResult);
        }
        return accum;
      }, []);
    }

    return {
      findOverlappingResults: findOverlappingResults,
      findNonOverlappingResults: findNonOverlappingResults
    };
  };

  return dependencies.concat(RepairService);
});
