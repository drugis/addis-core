'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'SparqlResource', 'MeasurementMomentService'];
    var PopulationCharacteristicService = function($q, StudyService, UUIDService, SparqlResource, MeasurementMomentService) {

      function isPopulationCharacteristic(node) {
        return node['@type'] === 'ontology:PopulationCharacteristic';
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
          measurementType: item.of_variable[0].measurementType
        };

        // if only one measurement moment is selected, it's a string, not an array
        if (Array.isArray(item.is_measured_at)) {
          frontEndItem.measuredAtMoments = _.map(item.is_measured_at, _.partial(findMeasurementForUri, measurementMoments));
        } else {
          frontEndItem.measuredAtMoments = [findMeasurementForUri(measurementMoments, item.is_measured_at)];
        }
        return frontEndItem;
      }

      function queryItems() {
        return MeasurementMomentService.queryItems().then(function(measurementMoments) {
          return StudyService.getStudy().then(function(study) {
            var populationCharacteristics = _.filter(study.has_outcome, isPopulationCharacteristic);
            return _.map(populationCharacteristics, _.partial(toFrontEnd, measurementMoments));
          });
        });
      }

      function measurementTypeToBackEnd(measurementType) {
        if (measurementType === 'ontology:continuous') {
          return [
            "http://trials.drugis.org/ontology#standard_deviation",
            "http://trials.drugis.org/ontology#mean",
            "http://trials.drugis.org/ontology#sample_size"
          ];
        } else if (measurementType === 'ontology:dichotomous') {
          return [
            "http://trials.drugis.org/ontology#sample_size",
            "http://trials.drugis.org/ontology#count"
          ];
        }
      }

      function toBackEnd(item) {
        return {
          '@type': 'ontology:PopulationCharacteristic',
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



      function addItem(item) {
        return StudyService.getStudy().then(function(study) {
          var newItem = toBackEnd(item);
          item['@id'] = 'http://trials.drugis.org/instances/' + UUIDService.generate();

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

      function editItem(item) {
        return StudyService.getStudy().then(function(study) {
          var backEndEditItem = toBackEnd(item);
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

      return {
        queryItems: queryItems,
        addItem: addItem,
        deleteItem: deleteItem,
        editItem: editItem
      };
    };
    return dependencies.concat(PopulationCharacteristicService);
  });
