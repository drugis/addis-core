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
      var sourceNonConformantResultsPromise = ResultsService.queryNonConformantMeasurementsByGroupUri(source.uri);
      var targetNonConformantResultsPromise = ResultsService.queryNonConformantMeasurementsByGroupUri(target.uri);
      return $q.all([sourceResultsPromise, targetResultsPromise, sourceNonConformantResultsPromise, targetNonConformantResultsPromise]).then(function(results) {
        var sourceResults = results[0];
        var targetResults = results[1];
        var sourceNonConformantResults = results[2];
        var targetNonConformantResults = results[3];
        return RepairService.mergeResults(targetUri, sourceResults, targetResults, isOverlappingGroupMeasurement, mergeProperty).then(function() {
          return RepairService.mergeResults(targetUri, sourceNonConformantResults, targetNonConformantResults, isOverlappingGroupMeasurement, mergeProperty).then(function() {
            return deleteItem(source);
          });
        });
      });
    }

    function isOverlappingGroupMeasurement(a, b) {
      return a.momentUri === b.momentUri &&
        a.outcomeUri === b.outcomeUri;
    }

    function isOverlappingNonConformantResultFunction(a, b) {
      return a.outcomeUri === b.outcomeUri &&
        a.comment === b.comment;
    }

    function hasOverlap(source, target) {
      var sourceUri = source.armURI || source.groupUri;
      var targetUri = target.armURI || target.groupUri;
      var sourceResultsPromise = ResultsService.queryResultsByGroup(sourceUri);
      var targetResultsPromise = ResultsService.queryResultsByGroup(targetUri);
      var sourceNonConformantResultsPromise = ResultsService.queryNonConformantMeasurementsByGroupUri(source.uri);
      var targetNonConformantResultsPromise = ResultsService.queryNonConformantMeasurementsByGroupUri(target.uri);

      return $q.all([sourceResultsPromise, targetResultsPromise, sourceNonConformantResultsPromise, targetNonConformantResultsPromise]).then(function(results) {
        return RepairService.findOverlappingResults(results[0], results[1], isOverlappingGroupMeasurement).length > 0 ||
          RepairService.findOverlappingResults(results[2], results[3], isOverlappingNonConformantResultFunction).length > 0;
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
