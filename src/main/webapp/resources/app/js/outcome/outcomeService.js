'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'MeasurementMomentService'];
    var OutcomeServiceService = function($q, StudyService, UUIDService, MeasurementMomentService) {

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


      return {
        queryItems: queryItems,
        addItem: addItem,
        deleteItem: deleteItem,
        editItem: editItem
      };
    };
    return dependencies.concat(OutcomeServiceService);
  });
