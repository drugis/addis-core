'use strict';
define(['lodash'], function(_) {
    var dependencies = ['$q', 'StudyService', 'UUIDService'];
    var EpochService = function($q, StudyService, UUIDService) {

      var INSTANCE_PREFIX = 'http://trials.drugis.org/instances/';

      function addPosition(item, index) {
        item.pos = index;
        return item;
      }

      function addIsPrimary(primaryEpochUri, item) {
        if (item.uri === primaryEpochUri) {
          item.isPrimary = true;
        } else {
          item.isPrimary = false;
        }
        return item;
      }

      function tofrontEnd(backendEpoch) {
        var frondEndEpoch = {
          uri: backendEpoch['@id'],
          label: backendEpoch.label,
          duration: backendEpoch.duration ? backendEpoch.duration : 'PT0S'
        };

        if (backendEpoch.comment) {
          frondEndEpoch.comment = backendEpoch.comment;
        }

        return frondEndEpoch;
      }

      function queryItems() {
        return StudyService.getStudy().then(function(study) {
          return study.has_epochs
            .map(tofrontEnd)
            .map(addPosition)
            .map(addIsPrimary.bind(this, study.has_primary_epoch));
        });
      }

      function addItem(item) {
        return StudyService.getStudy().then(function(study) {
          var newEpoch = {
            '@id': INSTANCE_PREFIX + UUIDService.generate(),
            '@type': 'ontology:Epoch',
            label: item.label,
            duration: item.duration
          };

          if (item.comment) {
            newEpoch.comment = item.comment;
          }

          if (item.isPrimaryEpoch) {
            study.has_primary_epoch = newEpoch['@id'];
          }

          study.has_epochs.push(newEpoch);
          return StudyService.save(study);
        });
      }

      function deleteItem(item) {
        return StudyService.getStudy().then(function(study) {

          if (study.has_primary_epoch === item.uri) {
            study.has_primary_epoch = undefined;
          }

          _.remove(study.has_epochs, function(epoch) {
            return epoch['@id'] === item.uri;
          });

          return StudyService.save(study);
        });
      }

      function editItem(newItem) {
        return StudyService.getStudy().then(function(study) {
          var editEpochIndex = _.findIndex(study.has_epochs, function(epoch) {
            return newItem.uri === epoch['@id'];
          });

          study.has_epochs[editEpochIndex].label = newItem.label;
          study.has_epochs[editEpochIndex].duration = newItem.duration;

          if (newItem.comment) {
            study.has_epochs[editEpochIndex].comment = newItem.comment;
          } else {
            delete study.has_epochs[editEpochIndex].comment;
          }

          if (study.has_primary_epoch === newItem.uri) {
            study.has_primary_epoch = undefined;
          }

          if (newItem.isPrimary) {
            study.has_primary_epoch = newItem.uri;
          }

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
    return dependencies.concat(EpochService);
  });
