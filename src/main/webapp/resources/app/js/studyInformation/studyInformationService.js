'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService'];
    var StudyInformationService = function($q, StudyService) {

      var URI_PROPERTIES = ['allocation', 'blinding', 'status'];

      function queryItems() {
        return StudyService.getStudy().then(function(study) {
          return [{
            allocation: study.has_allocation,
            blinding: study.has_blinding,
            status: study.status,
            objective: study.has_objective[0],
            numberOfCenters: study.has_number_of_centers
          }];
        });
      }

      function editItem(item) {
        angular.forEach(URI_PROPERTIES, function(property) {
          if (item[property]) {
            if (item[property].uri === 'unknown') {
              delete item[property];
            } else {
              item[property] = item[property].uri;
            }
          }
        });
        return StudyService.getStudy().then(function(study) {
          study.has_blinding = item.blinding;
          study.has_allocation = item.allocation;
          study.status = item.status;
          study.has_number_of_centers = item.numberOfCenters;
          if (item.objective && item.objective.comment) {
            study.has_objective[0] = {
              comment: item.objective.comment
            };
          } else {
            study.has_objective = [];
          }

          StudyService.save(study);

        });
      }

      return {
        queryItems: queryItems,
        editItem: editItem
      };
    };

    return dependencies.concat(StudyInformationService);
  });
