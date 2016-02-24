'use strict';
define(['angular-mocks'],
  function(angularMocks) {
    describe('study read only service', function() {

      var studyReadonlyService;

      beforeEach(function() {
        module('trialverse.study');
      });

      beforeEach(angularMocks.inject(function(StudyReadOnlyService) {
        studyReadonlyService = StudyReadOnlyService;
      }));

      describe('constructStudyDesignTableRows', function() {

        it('should build the desing table', function() {

          var groups = [{
            isArm: 'true',
            label: 'group label',
            numberOfParticipantsStarting: 2,
            groupUri: 'groupUri'
          }, {
            isArm: 'false'
          }];

          var epochs = [{
            epochUid: 'epochUid'
          }];

          var activities = [{
            activityApplications: [{
              epochUid: 'epochUid',
              armUid: 'groupUri'
            }]
          }];

          var rows = studyReadonlyService.constructStudyDesignTableRows(groups, epochs, activities);
          expect(rows.length).toBe(1);
          expect(rows[0].label).toBe('group label');
          expect(rows[0].numberOfParticipantsStarting).toBe(2);
        });
      });

      describe('flattenOutcomesToTableRows', function() {

        it('should build the desing table', function() {

          var studyGroups = [{
            isArm: 'true',
            label: 'group label',
            numberOfParticipantsStarting: 2,
            groupUri: 'groupUri'
          }, {
            isArm: 'false'
          }];

          var outcomes = [{
            studyDataMoments: [{
              relativeToAnchorOntology: 'relativeToAnchorOntology',
              relativeToEpochLabel: 'relativeToEpochLabel',
              timeOffsetDuration: 'timeOffsetDuration',
              studyDataValues: [{
                instanceUid: 'groupUri',
                count: 202
              }]
            }]
          }];

          var rows = studyReadonlyService.flattenOutcomesToTableRows(outcomes, studyGroups);
          expect(rows.length).toBe(1);
          expect(rows[0].studyDataValues[0].count).toBe(202);
        });
      });


    });
  });
