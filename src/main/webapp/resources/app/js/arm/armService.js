'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$q', 'StudyService', 'UUIDService', 'ResultsService'];
  var ArmService = function($q, StudyService, UUIDService, ResultsService) {

    function toFrontEnd(backEndArm) {
      var frontEndArm = {
        armURI: backEndArm['@id'],
        label: backEndArm.label,
      };

      if (backEndArm.comment) {
        frontEndArm.comment = backEndArm.comment;
      }

      return frontEndArm;
    }

    function toBackEnd(frontEndArm) {
      var backEndArm = {
        '@id': frontEndArm.armURI,
        label: frontEndArm.label,
      };

      if (frontEndArm.comment) {
        backEndArm.comment = frontEndArm.comment;
      }

      return backEndArm;
    }

    function queryItems() {
      return StudyService.getStudy().then(function(study) {
        return study.has_arm.map(toFrontEnd);
      });
    }

    function addItem(item) {
      return StudyService.getStudy().then(function(study) {
        var newArm = {
          '@id': 'http://trials.drugis.org/instances/' + UUIDService.generate(),
          '@type': 'ontology:Arm',
          label: item.label
        };

        if (item.comment) {
          newArm.comment = item.comment;
        }

        study.has_arm.push(newArm);
        return StudyService.save(study);
      });
    }

    function editItem(editArm) {
      return StudyService.getStudy().then(function(study) {
        study.has_arm = _.map(study.has_arm, function(arm) {
          if (arm['@id'] === editArm.armURI) {
            return toBackEnd(editArm);
          }
          return arm;
        });
        return StudyService.save(study);
      });
    }

    function reclassifyAsGroup(repairItem) {
      return StudyService.getStudy().then(function(study) {
        var armRemoved = _.remove(study.has_arm, function(arm) {
          return arm['@id'] === repairItem.armURI;
        })[0];

        armRemoved['@type'] = 'ontology:Group';

        study.has_group.push(armRemoved);

        return StudyService.save(study);
      });
    }

    function merge(source, target) {
      // fetch results data
      var sourceResultsPromise = ResultsService.queryResultsByGroup(source.armURI);
      var targetResultsPromise = ResultsService.queryResultsByGroup(target.armURI);

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
      var sourceResultsPromise = ResultsService.queryResultsByGroup(source.armURI);
      var targetResultsPromise = ResultsService.queryResultsByGroup(target.armURI);

      return $q.all([sourceResultsPromise, targetResultsPromise]).then(function(results) {
        return findOverlappingResults(results[0], results[1]).length > 0;
      });
    }

    function deleteItem(removeArm) {
      return StudyService.getStudy().then(function(study) {
        _.remove(study.has_arm, function(arm) {
          return arm['@id'] === removeArm.armURI;
        });
        return StudyService.save(study);
      });
    }

    return {
      queryItems: queryItems,
      addItem: addItem,
      editItem: editItem,
      reclassifyAsGroup: reclassifyAsGroup,
      hasOverlap: hasOverlap,
      merge: merge,
      deleteItem: deleteItem,
    };
  };

  return dependencies.concat(ArmService);
});
