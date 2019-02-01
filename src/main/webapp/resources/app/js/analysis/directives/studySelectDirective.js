'use strict';
define(['lodash'], function(_) {
  var dependencies = ['SingleStudyBenefitRiskService'];

  var StudySelectDirective = function(SingleStudyBenefitRiskService) {
    return {
      restrict: 'E',
      templateUrl: './studySelectDirective.html',
      scope: {
        studies: '=',
        checkErrors: '=',
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
          scope.studyOptions = createStudyOptions();
          var oldSelection = scope.selection.selectedStudy;
          if (oldSelection) {
            scope.selection.selectedStudy = _.find(scope.studyOptions, ['studyUri', oldSelection.studyUri]);
            scope.checkErrors();
          }
        });

        function createStudyOptions(){
          var studyOptions = SingleStudyBenefitRiskService.addMissingOutcomesToStudies(scope.studies, [scope.selection]);
          studyOptions = SingleStudyBenefitRiskService.addMissingValuesToStudies(scope.studies, scope.interventions, scope.selection);
          return SingleStudyBenefitRiskService.recalculateGroup(studyOptions, scope.interventions, scope.selection);
        }

        function onStudySelect(newSelection) {
          if (SingleStudyBenefitRiskService.isValidStudyOption(newSelection, scope.interventions, scope.selection)) {
            scope.onChange(newSelection);
          } else {
            scope.onChange();
          }
        }
      }
    };

  };

  return dependencies.concat(StudySelectDirective);

});
