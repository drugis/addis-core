'use strict';
define(['angular'], function() {
  var dependencies = [];
  var StudyDesignService = function() {

    var buildStudyDesignTable = function(treatmentActivities) {
      var table = {
        head: ['Arms', 'N'],
        body: []
      };

      _.each(treatmentActivities, function(activity) {
        // add headers
        if (!_.contains(table.head, activity.epochLabel)) {
          table.head.push(activity.epochLabel);
        }

        // add arm labels
        if (activity.armLabel) {
          table.body.push([{
            label: activity.armLabel
          }]);
        }
      });

      // fill table cells
      _.each(table.body, function(row) {
        var colCount = 2;
        _.each(treatmentActivities, function(activity) {
          if (row[0].label === activity.armLabel) {
            // treatment case
            row[colCount] = {
              label: activity.treatmentDrugLabel,
              fixedDosingPeriodicity: activity.fixedDosingPeriodicity,
              fixedUnitLabel: activity.fixedUnitLabel,
              fixedValue: activity.fixedValue,
              maxDosingPeriodicity: activity.maxDosingPeriodicity,
              maxUnitLabel: activity.maxUnitLabel,
              maxValue: activity.maxValue,
              minDosingPeriodicity: activity.minDosingPeriodicity,
              minUnitLabel: activity.minUnitLabel,
              minValue: activity.minValue
            };
            row[1] = {numberOfParticipantsStarting :activity.numberOfParticipantsStarting}    ;
            colCount++;
          } else if (!activity.treatmentDrugLabel) {
            // treatment case
            row[colCount] = {
              label: activity.treatmentActivityTypeLabel,
            };
            colCount++;
          }
        });
      });

      return table;
    };

    return {
      buildStudyDesignTable: buildStudyDesignTable
    };
  };
  return dependencies.concat(StudyDesignService);
});