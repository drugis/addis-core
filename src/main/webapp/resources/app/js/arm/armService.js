'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService'];
    var ArmService = function($q, StudyService, UUIDService) {

      function queryItems() {
        return StudyService.getStudy().then(function(study) {
          return study.has_arm;
        });
      }

      function addItem(arm) {
        return StudyService.getStudy().then(function(study) {
          arm['@id'] = 'http://trials.drugis.org/instances/' + UUIDService.generate();
          arm['@type'] = 'ontology:Arm';
          if (!study.has_arm) {
            stuy.has_arm = [];
          }
          study.has_arm.push(arm);
          StudyService.save(study);
        });
      }

      function editItem(editArm) {
        return StudyService.getStudy().then(function(study) {
          study.has_arm = _.map(study.has_arm, function(arm) {
            if (arm['@id'] === editArm['@id']) {
              return editArm;
            }
            return arm;
          });
          StudyService.save(study);
        });
      }

      function deleteItem(removeArm) {
        return StudyService.getStudy().then(function(study) {
          _.remove(study.has_arm, function(arm) {
            return arm['@id'] === removeArm['@id'];
          });
          StudyService.save(study);
        });
      }

      return {
        queryItems: queryItems,
        addItem: addItem,
        editItem: editItem,
        deleteItem: deleteItem,
      };
    };

    return dependencies.concat(ArmService);
  });
