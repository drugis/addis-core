'use strict';
define(['lodash'], function(_) {
  var dependencies = ['$q', 'StudyService', 'UUIDService', 'ResultsService'];
  var GroupService = function($q, StudyService, UUIDService, ResultsService) {

    function toFrontEnd(backEndGroup) {
      var frontEndGroup = {
        groupUri: backEndGroup['@id'],
        label: backEndGroup.label
      };

      if (backEndGroup['@type'] === 'ontology:StudyPopulation') {
        frontEndGroup.label = 'Overall population';
        frontEndGroup.disableEditing = true;
      }

      if (backEndGroup.comment) {
        frontEndGroup.comment = backEndGroup.comment;
      }

      return frontEndGroup;
    }

    function toBackEnd(frontEndGroup) {
      var backEndGroup = {
        '@id': frontEndGroup.groupUri,
        label: frontEndGroup.label,
      };

      if (frontEndGroup.comment) {
        backEndGroup.comment = frontEndGroup.comment;
      }

      return backEndGroup;
    }

    function addStudyPopulationIfMissing(study) {
      if (!study.has_included_population || study.has_included_population.length < 1) {
        study.has_included_population = [{
          '@id': 'http://trials.drugis.org/instances/' + UUIDService.generate(),
          '@type': 'ontology:StudyPopulation'
        }];
      }
      return study;
    }

    function queryItems() {
      return StudyService.getStudy().then(function(study) {
        // study might be old and not have groups and studypopulation
        // https://trello.com/c/owLPPtoX/1578-trialverse-support-for-non-arm-groups-3
        var studyWithPopulation = addStudyPopulationIfMissing(study);
        var groups = [];
        if(studyWithPopulation.has_group) {
          groups = studyWithPopulation.has_group;
        }
        return groups.concat(studyWithPopulation.has_included_population).map(toFrontEnd);
      });
    }

    function addItem(item) {
      return StudyService.getStudy().then(function(study) {
        var newGroup = {
          '@id': 'http://trials.drugis.org/instances/' + UUIDService.generate(),
          '@type': 'ontology:Group',
          label: item.label
        };

        if (item.comment) {
          newGroup.comment = item.comment;
        }

        study.has_group.push(newGroup);
        return StudyService.save(study);
      });
    }

    function editItem(editGroup) {
      return StudyService.getStudy().then(function(study) {
        study.has_group = _.map(study.has_group, function(group) {
          if (group['@id'] === editGroup.groupUri) {
            return toBackEnd(editGroup);
          }
          return group;
        });
        return StudyService.save(study);
      });
    }

    function reclassifyAsArm(repairItem) {
      return StudyService.getStudy().then(function(study) {
        var groupRemoved = _.remove(study.has_group, function(group) {
          return group['@id'] === repairItem.groupUri;
        })[0];

        groupRemoved['@type'] = 'ontology:Arm';

        study.has_arm.push(groupRemoved);

        return StudyService.save(study);
      });
    }

    function merge(source, target) {
      // fetch results data
      var sourceResultsPromise = ResultsService.queryResultsByGroup(source.groupUri);
      var targetResultsPromise = ResultsService.queryResultsByGroup(target.groupUri);

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
      var sourceResultsPromise = ResultsService.queryResultsByGroup(source.groupUri);
      var targetResultsPromise = ResultsService.queryResultsByGroup(target.groupUri);

      return $q.all([sourceResultsPromise, targetResultsPromise]).then(function(results) {
        return findOverlappingResults(results[0], results[1]).length > 0;
      });
    }

    function deleteItem(removeGroup) {
      return StudyService.getStudy().then(function(study) {
        _.remove(study.has_group, function(group) {
          return group['@id'] === removeGroup.groupUri;
        });
        return StudyService.save(study);
      });
    }

    return {
      queryItems: queryItems,
      addItem: addItem,
      editItem: editItem,
      reclassifyAsArm: reclassifyAsArm,
      merge: merge,
      hasOverlap: hasOverlap,
      deleteItem: deleteItem,
    };
  };

  return dependencies.concat(GroupService);
});
