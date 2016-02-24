'use strict';
define(['lodash'], function(_) {
    var dependencies = [];
    var StudyReadOnlyService = function() {

      function constructStudyDesignTableRows(studyGroups, studyEpochs, treatmentActivities) {
        var rows = [];
        var arms = studyGroups.filter(function(group) {
          return group.isArm === 'true'; //evil, moe haha ha
        });
        _.each(arms, function(group) {
          var row = {};
          row.label = group.label;
          row.numberOfParticipantsStarting = group.numberOfParticipantsStarting;
          row.epochCells = [];

          _.each(studyEpochs, function(epoch) {
            row.epochCells.push(getCellTreatments(epoch.epochUid, group.groupUri, treatmentActivities)[0]);
          });
          rows.push(row);
        });
        return rows;
      }

      function flattenOutcomesToTableRows(outcomes, studyGroups) {
        var rows = [];

        _.each(outcomes, function(outcome) {
          _.each(outcome.studyDataMoments, function(moment) {
            var row = {};
            row.studyDataTypeLabel = outcome.studyDataTypeLabel;
            row.relativeToAnchorOntology = moment.relativeToAnchorOntology;
            row.relativeToEpochLabel = moment.relativeToEpochLabel;
            row.timeOffsetDuration = moment.timeOffsetDuration;

            row.studyDataValues = studyGroups.map(function(group) {
              var groupValue = moment.studyDataValues.find(function(value) {
                return value.instanceUid === group.groupUri;
              });
              if (groupValue === undefined) {
                return {
                  instanceUid: group.groupUri,
                  label: 'no value'
                };
              }
              return groupValue;
            });

            rows.push(row);
          });

        });
        return rows;
      }

      function getCellTreatments(epochUid, groupUri, treatmentActivities) {
        return _.filter(treatmentActivities, function(activity) {
          var cellHasApplication = _.find(activity.activityApplications, function(application) {
            return (application.epochUid === epochUid && application.armUid === groupUri);
          });
          if (cellHasApplication) {
            return activity;
          }
        });
      }


      return {
        constructStudyDesignTableRows: constructStudyDesignTableRows,
        flattenOutcomesToTableRows: flattenOutcomesToTableRows
      };
    };

    return dependencies.concat(StudyReadOnlyService);
  });
