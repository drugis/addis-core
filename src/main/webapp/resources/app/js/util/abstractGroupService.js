'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$q', 'StudyService', 'ResultsService'];
  var AbstractGroupService = function($q, StudyService, ResultsService) {

    function merge(source, target) {
      // fetch results data
      var sourceResultsPromise = ResultsService.queryResultsByGroup(source.armURI || source.groupUri);
      var targetResultsPromise = ResultsService.queryResultsByGroup(target.armURI || target.groupUri);

      return $q.all([sourceResultsPromise, targetResultsPromise]).then(function(results) {
        var overlappingResults = findOverlappingResults(results[0], results[1]);
        var nonOverlappingResults = findNonOverlappingResults(results[0], results[1]);

        return StudyService.getJsonGraph().then(function(graph) {
          // remove the overlapping results
          _.forEach(overlappingResults, function(overlappingResult) {
            _.remove(graph, function(node) {
              return overlappingResult.instance === node['@id'];
            });
          });

          // move non overlapping results
          _.forEach(nonOverlappingResults, function(nonOverlappingResult) {
            // remove the overlapping results
            var resultNode = _.find(graph, function(node) {
              return nonOverlappingResult.instance === node['@id'];
            });
            resultNode.of_group = target.armURI || target.groupUri;
          });

          // store the merged results
          return StudyService.saveJsonGraph(graph).then(function() {
            // remove the merged arm
            return deleteItem(source);
          });
        });
      });
    }

    function isOverlappingGroupMeasurement(a, b) {
      return a.momentUri === b.momentUri &&
        a.outcomeUri === b.outcomeUri;
    }

    function findOverlappingResults(sourceResults, targetResults) {
      return _.reduce(sourceResults, function(accum, sourceResult) {
        var targetResult = _.find(targetResults, function(targetResult) {
          return isOverlappingGroupMeasurement(sourceResult, targetResult);
        });
        if (targetResult) {
          accum.push(sourceResult);
        }
        return accum;
      }, []);
    }

    function findNonOverlappingResults(sourceResults, targetResults) {
      return _.reduce(sourceResults, function(accum, sourceResult) {
        var targetResult = _.find(targetResults, function(targetResult) {
          return isOverlappingGroupMeasurement(sourceResult, targetResult);
        });
        if (!targetResult) {
          accum.push(sourceResult);
        }
        return accum;
      }, []);
    }

    function hasOverlap(source, target) {
      var sourceResultsPromise = ResultsService.queryResultsByGroup(source.armURI || source.groupUri);
      var targetResultsPromise = ResultsService.queryResultsByGroup(target.armURI || target.groupUri);

      return $q.all([sourceResultsPromise, targetResultsPromise]).then(function(results) {
        return findOverlappingResults(results[0], results[1]).length > 0;
      });
    }

    function deleteItem(removeGroup) {
      var subType, uriKey;
      if(removeGroup.armURI) {
        subType = 'has_arm';
        uriKey = 'armURI';
      } else {
        subType = 'has_group';
        uriKey = 'groupUri';
      }
      return StudyService.getStudy().then(function(study) {
        _.remove(study[subType], function(group) {
          return group['@id'] === removeGroup[uriKey];
        });
        return StudyService.save(study);
      });
    }

    return {
      merge: merge,
      hasOverlap: hasOverlap,
      deleteItem: deleteItem,
    };
  };

  return dependencies.concat(AbstractGroupService);
});
