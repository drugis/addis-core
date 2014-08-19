'use strict';
define(['angular'], function() {
  var dependencies = [];
  var StudyDesignService = function() {

    var buildStudyDesignTable = function(epochs, arms, treatmentActivities) {
      var table = {
        head: [{label: 'Arms'}, {label: 'Participants starting'}],
        body: []
      };

      // setup cols
      table.head.concat(epochs);

      /// setup rows
      table.body.concat(_.map(arms, function(arm) {
        return [arm.label, arms.numberOfParticipantsStarting];
      }));


      // fillout cells
      _.each(table.body, function(row) {
        _.each(row, function(column) {
          if(column.epochUid) {
            var epochTreatments = _.filter(treatmentActivities, function(activity) {
              return column.epochUid === activity.epochUid;
            });

            if(epochTreatments.administeredDrugs.length > 0) {
              column.push(epochTreatments.administeredDrugs);

            } else {
              column.push(epochTreatments[0].activityType);
            }
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

