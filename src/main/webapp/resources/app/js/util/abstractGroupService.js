'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$q', 'StudyService', 'ResultsService', 'RepairService'];
  var AbstractGroupService = function($q, StudyService, ResultsService, RepairService) {

    function merge(source, target) {
      var sourceUri = source.armURI || source.groupUri;
      var targetUri = target.armURI || target.groupUri;
      var mergeProperty = 'of_group';
      var sourceResultsPromise = ResultsService.queryResultsByGroup(sourceUri);
      var targetResultsPromise = ResultsService.queryResultsByGroup(targetUri);
      return $q.all([sourceResultsPromise, targetResultsPromise]).then(function(results) {
        var sourceResults = results[0];
        var targetResults = results[1];
        return RepairService.mergeResults(targetUri, sourceResults, targetResults, isOverlappingGroupMeasurement, mergeProperty).then(function() {
          return deleteItem(source);
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
      if (removeGroup.armURI) {
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
