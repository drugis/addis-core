'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$q', 'StudyService', 'ResultsService'];
  var RepairService = function($q, StudyService, ResultsService) {

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

    function mergeResults(targetUri, sourceResults, targetResults, overlapFunction, mergeProperty) {

      var overlappingResults = findOverlappingResults(sourceResults, targetResults, overlapFunction);
      var nonOverlappingResults = findNonOverlappingResults(sourceResults, targetResults, overlapFunction);

      return StudyService.getJsonGraph().then(function(graph) {
        // remove the overlapping results
        _.forEach(overlappingResults, function(overlappingResult) {
          _.remove(graph, function(node) {
            return overlappingResult.instance === node['@id'];
          });
        });

        // move non overlapping results
        _.forEach(nonOverlappingResults, function(nonOverlappingResult) {
          var resultNode = _.find(graph, function(node) {
            return nonOverlappingResult.instance === node['@id'];
          });
          resultNode[mergeProperty] = targetUri;
        });

        // store the merged results
        return StudyService.saveJsonGraph(graph);
      });
    }

    return {
      findOverlappingResults: findOverlappingResults,
      findNonOverlappingResults: findNonOverlappingResults,
      mergeResults: mergeResults
    };
  };

  return dependencies.concat(RepairService);
});
