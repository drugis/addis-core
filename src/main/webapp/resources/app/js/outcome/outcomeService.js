'use strict';
define(['angular', 'lodash'],
  function(angular, _) {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'MeasurementMomentService', 'ResultsService', 'RepairService'];
    var OutcomeServiceService = function($q, StudyService, UUIDService, MeasurementMomentService, ResultsService, RepairService) {

      function isOverlappingResultFunction(a, b) {
        return a.armUri === b.armUri &&
          a.momentUri === b.momentUri;
      }

      function findMeasurementForUri(measurementMoments, measurementMomentUri) {
        return _.find(measurementMoments, function(moment) {
          return measurementMomentUri === moment.uri;
        });
      }

      function toFrontEnd(measurementMoments, item) {
        var frontEndItem = {
          uri: item['@id'],
          label: item.label,
          measurementType: item.of_variable[0].measurementType,
          measuredAtMoments: [],
          conceptMapping: item.of_variable[0].sameAs
        };

        // if only one measurement moment is selected, it's a string, not an array
        if (Array.isArray(item.is_measured_at)) {
          frontEndItem.measuredAtMoments = _.map(item.is_measured_at, _.partial(findMeasurementForUri, measurementMoments));
        } else {
          if (item.is_measured_at) {
            frontEndItem.measuredAtMoments = [findMeasurementForUri(measurementMoments, item.is_measured_at)];
          }
        }
        return frontEndItem;
      }

      function toBackEnd(item, type) {
        return {
          '@type': type,
          '@id': item.uri,
          is_measured_at: item.measuredAtMoments.length === 1 ? item.measuredAtMoments[0].uri : _.map(item.measuredAtMoments, 'uri'),
          label: item.label,
          of_variable: [{
            '@type': 'ontology:Variable',
            'measurementType': item.measurementType,
            'label': item.label
          }],
          has_result_property: measurementTypeToBackEnd(item.measurementType)
        };
      }

      function measurementTypeToBackEnd(measurementType) {
        if (measurementType === 'ontology:continuous') {
          return [
            'http://trials.drugis.org/ontology#standard_deviation',
            'http://trials.drugis.org/ontology#mean',
            'http://trials.drugis.org/ontology#sample_size'
          ];
        } else if (measurementType === 'ontology:dichotomous') {
          return [
            'http://trials.drugis.org/ontology#sample_size',
            'http://trials.drugis.org/ontology#count'
          ];
        }
      }

      function queryItems(typeCheckFunction) {
        return MeasurementMomentService.queryItems().then(function(measurementMoments) {
          return StudyService.getStudy().then(function(study) {
            var outcomes = _.filter(study.has_outcome, typeCheckFunction);
            return _.map(outcomes, _.partial(toFrontEnd, measurementMoments));
          });
        });
      }

      function addItem(item, type) {
        return StudyService.getStudy().then(function(study) {
          var newItem = toBackEnd(item, type);
          newItem['@id'] = 'http://trials.drugis.org/instances/' + UUIDService.generate();

          study.has_outcome.push(newItem);
          return StudyService.save(study);
        });
      }

      function deleteItem(item) {
        return StudyService.getStudy().then(function(study) {
          study.has_outcome = _.reject(study.has_outcome, function(node) {
            return node['@id'] === item.uri;
          });
          return StudyService.save(study);
        });
      }

      function editItem(item, type) {
        return StudyService.getStudy().then(function(study) {
          var backEndEditItem = toBackEnd(item, type);
          study.has_outcome = _.map(study.has_outcome, function(node) {
            if (node['@id'] === item.uri) {
              return backEndEditItem;
            } else {
              return node;
            }
          });
          return StudyService.save(study);
        });
      }

      function moveToNewOutcome(variableType, newOutcomeName, baseOutcome, nonConformantMeasurementUrisToMove) {

        var newUri = 'http://trials.drugis.org/instances/' + UUIDService.generate();
        var newOutcome = angular.copy(baseOutcome);
        newOutcome.uri = newUri;
        newOutcome.label = newOutcomeName;
        newOutcome.measuredAtMoments = [];
        var backEndOutcome = toBackEnd(newOutcome, 'ontology:' + variableType);
        var measurementsById = _.keyBy(nonConformantMeasurementUrisToMove, _.identity);

        return StudyService.getJsonGraph().then(function(graph) {
          var study = _.find(graph, ResultsService.isStudyNode);
          study.has_outcome.push(backEndOutcome);

          graph = _.map(graph, function(node) {
            var nodeId = node['@id'];
            if (measurementsById[nodeId]) {
              node.of_outcome = newUri;
            }
            return node;
          });
          return StudyService.saveJsonGraph(graph);
        });
      }

      function hasOverlap(source, target) {
        var sourceResultsPromise = ResultsService.queryResultsByOutcome(source.uri);
        var targetResultsPromise = ResultsService.queryResultsByOutcome(target.uri);

        return $q.all([sourceResultsPromise, targetResultsPromise]).then(function(results) {
          return RepairService.findOverlappingResults(results[0], results[1], isOverlappingResultFunction).length > 0;
        });
      }

      function hasDifferentType(source, target) {
        return source.measurementType !== target.measurementType;
      }

      /*
       ** Moves the measurementMoments that are on the source but not on the target to the target
       ** and then updated the graph
       */
      function mergeMeasurementMoments(source, target, type) {
        var newMoments = _.reduce(source.measuredAtMoments, function(accum, sourceMeasurementMoment) {
          var isMeassuredByTarget = _.find(target.measuredAtMoments, function(targetMeasurementMoment) {
            return targetMeasurementMoment.uri === sourceMeasurementMoment.uri;
          });

          if (!isMeassuredByTarget) {
            accum.push(sourceMeasurementMoment);
          }
          return accum;
        }, []);

        target.measuredAtMoments = _.compact([].concat(target.measuredAtMoments).concat(newMoments));
        return editItem(target, type);
      }

      function merge(source, target, type) {
        var sourceUri = source.uri;
        var targetUri = target.uri;
        var mergeProperty = 'of_outcome';
        var sourceResultsPromise = ResultsService.queryResultsByOutcome(sourceUri);
        var targetResultsPromise = ResultsService.queryResultsByOutcome(targetUri);
        return $q.all([sourceResultsPromise, targetResultsPromise]).then(function(results) {
          var sourceResults = results[0];
          var targetResults = results[1];
          return RepairService.mergeResults(targetUri, sourceResults, targetResults, isOverlappingResultFunction, mergeProperty).then(function() {
            return deleteItem(source).then(function() {
              return mergeMeasurementMoments(source, target, type);
            });
          });
        });
      }


      return {
        queryItems: queryItems,
        addItem: addItem,
        deleteItem: deleteItem,
        editItem: editItem,
        toBackEnd: toBackEnd,
        moveToNewOutcome: moveToNewOutcome,
        hasOverlap: hasOverlap,
        hasDifferentType: hasDifferentType,
        merge: merge
      };
    };
    return dependencies.concat(OutcomeServiceService);
  });
