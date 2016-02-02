'use strict';
define([],
  function() {
    var dependencies = ['$q', 'StudyService', 'UUIDService'];
    var PopulationInformationService = function($q, StudyService, UUIDService) {

      var INSTANCE_PREFIX = 'http://trials.drugis.org/instances/';

      // Each study can have a maximun of 1 populationInformation items
      function queryItems() {
        return StudyService.getStudy().then(function(study) {
          var item = {
            indication: {
              label: undefined
            },
            eligibilityCriteria: {
              label: undefined
            }
          };

          if (study.has_indication) {
            item.indication = study.has_indication[0];
          }

          if (study.has_eligibility_criteria.length > 0 && study.has_eligibility_criteria[0]) {
            item.eligibilityCriteria = {
              label: study.has_eligibility_criteria[0].comment
            };
          }

          return [item];
        });
      }


      function editItem(item) {
        return StudyService.getStudy().then(function(study) {

          if (item.indication) {
            if (study.has_indication[0] === undefined) {
              study.has_indication = [{
                '@id': INSTANCE_PREFIX + UUIDService.generate()
              }];
            }
            study.has_indication[0].label = item.indication.label;
          }

          if (item.eligibilityCriteria) {
            if (study.has_eligibility_criteria[0] === undefined) {
              study.has_eligibility_criteria = [{
                '@id': INSTANCE_PREFIX + UUIDService.generate()
              }];
            }
            study.has_eligibility_criteria[0].comment = item.eligibilityCriteria.label;
          }

          StudyService.save(study);
        });
      }

      return {
        queryItems: queryItems,
        editItem: editItem,
        INSTANCE_PREFIX: INSTANCE_PREFIX
      };
    };
    return dependencies.concat(PopulationInformationService);
  });
