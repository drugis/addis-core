'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService'];
    var StudyInformationService = function($q, StudyService) {

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
        function dropdownValueToUri(property) {
          if (property) {
            if (property.uri === 'unknown') {
              return undefined;
            } else {
              return property.uri;
            }
          }
        }
        return StudyService.getStudy().then(function(study) {
          study.has_blinding = dropdownValueToUri(item.blinding);
          study.has_allocation = dropdownValueToUri(item.allocation);
          study.status = dropdownValueToUri(item.status);
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
