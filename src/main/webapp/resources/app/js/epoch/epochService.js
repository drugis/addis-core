'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService'];
    var EpochService = function($q, StudyService, UUIDService) {

      var INSTANCE_PREFIX = 'http://trials.drugis.org/instances/';

      function addPosition(item, index) {
        item.pos = index;
        return item;
      }

      function addIsPrimary(primaryEpochUri, item) {
        if (item['@id'] === primaryEpochUri) {
          item.isPrimary = true;
        } else {
          item.isPrimary = false;
        }
        return item;
      }

      function queryItems() {
        return StudyService.getStudy().then(function(study) {
          return study
            .has_epochs
            .map(addPosition)
            .map(addIsPrimary.bind(this, study.has_primary_epoch));

        });
      }

      function addItem(item) {
        return StudyService.getStudy().then(function(study) {
          var newEpoch = {
            '@id': INSTANCE_PREFIX + UUIDService.generate(),
            label: item.label,
            duration: item.duration
          };

          if(item.comment) {
            newEpoch.comment = item.comment;
          }

          if(item.isPrimaryEpoch) {
            study.has_primary_epoch = newEpoch['@id'];
          }

          study.has_epochs.push(newEpoch)
          return StudyService.save(study);
        });
      }

      function deleteItem(item) {
        return deleteEpochRaw.then(function(deleteQueryRaw) {
          var deleteQuery = deleteQueryRaw.replace(/\$URI/g, item.uri);
          return StudyService.doModifyingQuery(deleteQuery);
        });
      }

      function editItem(newItem) {
        return StudyService.getStudy().then(function(study) {
          var editEpochIndex = _.findIndex(study.has_epochs, function(epoch) {
            return newItem['@id'] === epoch['@id'];
          });

          study.has_epochs[editEpochIndex].label = newItem.label;
          study.has_epochs[editEpochIndex].duration = newItem.duration;

          if(newItem.comment) {
            study.has_epochs[editEpochIndex].comment = newItem.comment;
          } else {
            delete study.has_epochs[editEpochIndex].comment;
          }

          if(study.has_primary_epoch === newItem['@id']) {
            study.has_primary_epoch = undefined;
          }

          if(newItem.isPrimaryEpoch) {
            study.has_primary_epoch = newItem['@id'];
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