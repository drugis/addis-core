'use strict';
define(['lodash'], function(_) {
  var dependencies = [];
  var DatasetService = function() {
    
    function filterStudies(studies, filterSelections) {
      function studyLacksSelector(studyList, filterList) {
        return _.find(filterList, function(filterItem) {
          return !_.includes(studyList, filterItem['@id']);
        });
      }

      return _.reject(studies, function(study) {
        return studyLacksSelector(study.drugUris, filterSelections.drugs) ||
          studyLacksSelector(study.outcomeUris, filterSelections.variables);
      });
    }

    return {
      filterStudies: filterStudies
    };

  };

  return dependencies.concat(DatasetService);
});
