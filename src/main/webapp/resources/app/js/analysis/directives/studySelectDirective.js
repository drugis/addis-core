'use strict';
define(['lodash'], function(_) {
  var dependencies = ['SingleStudyBenefitRiskService'];

  var StudySelectDirective = function(SingleStudyBenefitRiskService) {
    return {
      restrict: 'E',
      templateUrl: './studySelectDirective.html',
      scope: {
        studies: '=',
        outcome: '=',
        interventions: '=',
        selection: '=',
        onChange: '=',
        editMode: '='
      },
      link: function(scope) {
        scope.onStudySelect = onStudySelect;

        if (!scope.selection) {
          scope.selection = {};
        }
        scope.$watch('studies', function() {
          var studyOptions = SingleStudyBenefitRiskService.addMissingOutcomesToStudies(scope.studies, [scope.outcome]);
          scope.studyOptions = SingleStudyBenefitRiskService.recalculateGroup(studyOptions);
          var oldSelection = scope.selection.selectedStudy;
          if (oldSelection) {
            scope.selection.selectedStudy = _.find(scope.studyOptions, ['studyUri', oldSelection.studyUri]);
          }
        });

        function onStudySelect(newSelection) {
          if (isValidStudy(newSelection)) {
            scope.onChange(newSelection);
          } else {
            scope.onChange();
          }
        }

        function isValidStudy(study) {
          return study &&
            (!study.missingInterventions || study.missingInterventions.length === 0) &&
            (!study.missingOutcomes || study.missingOutcomes.length === 0) &&
            !study.hasMatchedMixedTreatmentArm;
        }

      }
    };

  };

  return dependencies.concat(StudySelectDirective);

});
