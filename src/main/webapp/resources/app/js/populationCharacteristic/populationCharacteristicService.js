'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'SparqlResource', 'MeasurementMomentService'];
    var PopulationCharacteristicService = function($q, StudyService, UUIDService, SparqlResource, MeasurementMomentService) {

      function isPopulationCharacteristic(node) {
        return node['@type'] === 'ontology:PopulationCharacteristic';
      }

      function toFrontEnd(measurementMoments, item) {
        function findMeasurementForUri(measurementMomentUri) {
          return _.find(measurementMoments, function(moment) {
            return measurementMomentUri === moment.uri;
          });
        }

        var frontEndItem = {
          uri: item['@id'],
          label: item.label,
          measurementType: item.of_variable[0].measurementType
        };

        if (Array.isArray(item.is_measured_at)) {
          frontEndItem.measuredAtMoments = _.map(item.is_measured_at, findMeasurementForUri);
        } else {
          frontEndItem.measuredAtMoments = [findMeasurementForUri(item.is_measured_at)];
        }
        return frontEndItem;
      }

      function queryItems() {
        return MeasurementMomentService.queryItems().then(function(measurementMoments) {
          return StudyService.getStudy().then(function(study) {
            return _.map(_.filter(study.has_outcome, isPopulationCharacteristic), _.partial(toFrontEnd, measurementMoments));
          });
        });
      }

      // function queryItems() {
      //   var items, measuredAtMoments, measurementMoments;

      //   var queryItemsPromise = populationCharacteristicsQuery.then(function(query) {
      //     return StudyService.doNonModifyingQuery(query).then(function(result) {
      //       items = result;
      //     });
      //   });

      //   var measuredAtQueryPromise = queryMeasuredAtTemplate.then(function(query) {
      //     return StudyService.doNonModifyingQuery(query).then(function(result) {
      //       measuredAtMoments = result;
      //     });
      //   });

      //   var measurementMomentsPromise = MeasurementMomentService.queryItems().then(function(result) {
      //     measurementMoments = result;
      //   });

      //   return $q.all([queryItemsPromise, measuredAtQueryPromise, measurementMomentsPromise]).then(function() {
      //     return _.map(items, function(item) {
      //       var filtered = _.filter(measuredAtMoments, function(measuredAtMoment) {
      //         return item.uri === measuredAtMoment.itemUri;
      //       });

      //       item.measuredAtMoments = _.map(_.pluck(filtered, 'measurementMoment'), function(measurementMomentUri) {
      //         return _.find(measurementMoments, function(moment) {
      //           return measurementMomentUri === moment.uri;
      //         });
      //       });
      //       return item;
      //     });
      //   });
      // }

      function toBackEnd(item) {
        return {
          '@type': 'ontology:PopulationCharacteristic',
          is_measured_at: item.measuredAtMoments.length === 1 ? item.measuredAtMoments[0].uri : _.map(item.measuredAtMoments, 'uri'),
          label: item.label,
          of_variable: [{
            '@type': 'ontology:Variable',
            'measurementType': item.measurementType,
            'label': item.label
          }]
        };
      }

      function addItem(item) {
        return StudyService.getStudy().then(function(study) {
          var newItem = toBackEnd(item);
          item['@id'] = 'http://trials.drugis.org/instances/' + UUIDService.generate();

          study.has_outcome.push(newItem);
          return StudyService.save(study);
        });
      }

      // function addItem(item) {
      //   var newItem = angular.copy(item);
      //   newItem.uuid = UUIDService.generate();
      //   newItem.uri = 'http://trials.drugis.org/instances/' + newItem.uuid;
      //   var stringToInsert = buildInsertMeasuredAtBlock(newItem);

      //   var addItemPromise = addPopulationCharacteristicQueryRaw.then(function(query) {
      //     var addPopulationCharacteristicQuery = fillInTemplate(query, newItem);
      //     return StudyService.doModifyingQuery(addPopulationCharacteristicQuery).then(function() {
      //       return OutcomeService.setOutcomeProperty(newItem);
      //     });
      //   });

      //   var addMeasuredAtPromise = addTemplateRaw.then(function(query) {
      //     var addMeasuredAtQuery = query.replace('$insertBlock', stringToInsert);
      //     return StudyService.doModifyingQuery(addMeasuredAtQuery);
      //   });

      //   return $q.all([addItemPromise, addMeasuredAtPromise]);
      // }

      function deleteItem(item) {
        return deletePopulationCharacteristicRaw.then(function(deleteQueryRaw) {
          return StudyService.doModifyingQuery(deleteQueryRaw.replace(/\$URI/g, item.uri));
        });
      }

      function editItem(item) {
        var newItem = angular.copy(item);
        newItem.measurementMomentBlock = buildInsertMeasuredAtBlock(newItem);
        return editPopulationCharacteristicRaw.then(function(editQueryRaw) {
          var editQuery = fillInTemplate(editQueryRaw, newItem);
          return StudyService.doModifyingQuery(editQuery).then(function() {
            return OutcomeService.setOutcomeProperty(item);
          });
        });
      }

      function buildInsertMeasuredAtBlock(item) {
        return _.reduce(item.measuredAtMoments, function(accumulator, measuredAtMoment) {
          return accumulator + ' <' + item.uri + '> ontology:is_measured_at <' + measuredAtMoment.uri + '> .';
        }, '');
      }

      function fillInTemplate(template, item) {
        return template
          .replace(/\$UUID/g, item.uuid)
          .replace('$label', item.label)
          .replace('$measurementType', item.measurementType)
          .replace('$insertMeasurementMomentBlock', item.measurementMomentBlock)
          .replace(/\$URI/g, item.uri);
      }

      return {
        queryItems: queryItems,
        addItem: addItem,
        deleteItem: deleteItem,
        editItem: editItem
      };
    };
    return dependencies.concat(PopulationCharacteristicService);
  });
