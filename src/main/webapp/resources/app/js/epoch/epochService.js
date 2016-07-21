'use strict';
define(['lodash'], function(_) {
    var dependencies = ['$q', 'StudyService', 'UUIDService', 'RdfListService'];
    var EpochService = function($q, StudyService, UUIDService, RdfListService) {

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
        return StudyService.getJsonGraph().then(function(graph) {
          var study = StudyService.findStudyNode(graph);
          return RdfListService.flattenList(study.has_epochs)
            .map(tofrontEnd)
            .map(addPosition)
            .map(addIsPrimary.bind(this, study.has_primary_epoch));
        });
      }

      function addItem(item) {
        return StudyService.getJsonGraph().then(function(graph) {
          var study = StudyService.findStudyNode(graph);
          var newId = INSTANCE_PREFIX + UUIDService.generate();
          var newEpoch = {
            '@id': newId,
            '@type': 'ontology:Epoch',
            label: item.label,
            duration: item.duration
          };

          if (item.comment) {
            newEpoch.comment = item.comment;
          }

          if (item.isPrimaryEpoch) {
            study.has_primary_epoch = newId;
          }

          var epochs = RdfListService.flattenList(study.has_epochs);
          epochs.push(newEpoch);
          study.has_epochs = RdfListService.unFlattenList(epochs);
          return StudyService.saveJsonGraph(graph);
        });
      }

      function deleteItem(item) {
        return StudyService.getJsonGraph().then(function(graph) {
          var study = StudyService.findStudyNode(graph);

          if (study.has_primary_epoch === item.uri) {
            study.has_primary_epoch = undefined;
          }

          var epochs = RdfListService.flattenList(study.has_epochs);

          epochs = _.reject(epochs, function(epoch) {
            return epoch['@id'] === item.uri;
          });

          study.has_epochs = RdfListService.unFlattenList(epochs);

          return StudyService.saveJsonGraph(graph);
        });
      }

      function editItem(newItem) {
        return StudyService.getJsonGraph().then(function(graph) {
          var study = StudyService.findStudyNode(graph);
          var epochs = RdfListService.flattenList(study.has_epochs);
          var editEpochIndex = _.findIndex(epochs, function(epoch) {
            return newItem.uri === epoch['@id'];
          });

          epochs[editEpochIndex].label = newItem.label;
          epochs[editEpochIndex].duration = newItem.duration;

          if (newItem.comment) {
            epochs[editEpochIndex].comment = newItem.comment;
          } else {
            delete epochs[editEpochIndex].comment;
          }

          if (study.has_primary_epoch === newItem.uri) {
            study.has_primary_epoch = undefined;
          }

          if (newItem.isPrimary) {
            study.has_primary_epoch = newItem.uri;
          }
          study.has_epochs = RdfListService.unFlattenList(epochs);

          return StudyService.saveJsonGraph(graph);
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
