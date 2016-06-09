'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$q', 'StudyService', 'ResultsService', 'RepairService'];
  var AbstractGroupService = function($q, StudyService, ResultsService, RepairService) {

    function merge(source, target) {
      // fetch results data
      var sourceResultsPromise = ResultsService.queryResultsByGroup(source.armURI || source.groupUri);
      var targetResultsPromise = ResultsService.queryResultsByGroup(target.armURI || target.groupUri);

      return $q.all([sourceResultsPromise, targetResultsPromise]).then(function(results) {
        var overlappingResults = RepairService.findOverlappingResults(results[0], results[1], isOverlappingGroupMeasurement);
        var nonOverlappingResults = RepairService.findNonOverlappingResults(results[0], results[1], isOverlappingGroupMeasurement);

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

    function hasOverlap(source, target) {
      var sourceResultsPromise = ResultsService.queryResultsByGroup(source.armURI || source.groupUri);
      var targetResultsPromise = ResultsService.queryResultsByGroup(target.armURI || target.groupUri);

      return $q.all([sourceResultsPromise, targetResultsPromise]).then(function(results) {
        return RepairService.findOverlappingResults(results[0], results[1], isOverlappingGroupMeasurement).length > 0;
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
