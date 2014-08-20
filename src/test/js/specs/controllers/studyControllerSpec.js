define(['angular', 'angular-mocks', 'controllers'],
  function() {
    describe("The studyController", function() {
      var $scope, $q, $stateParams, TrialverseResource, StudyDetailsResource,
        StudyTreatmentActivityResource, StudyArmResource, StudyEpochResource,
        StudyPopulationCharacteristicsResource, StudyEndpointsResource, StudyAdverseEventsResource,
        treatmentActivity;

      beforeEach(module('addis.controllers'));

      beforeEach(inject(function($controller) {
        treatmentActivity = {
          activityApplications: [{
            epochUid: 'epcohUid',
            armUid: 'armUid'
          }]
        };
        var treatmentActivities = [treatmentActivity];

        TrialverseResource = jasmine.createSpyObj('TrialverseResource', ['get']);
        StudyDetailsResource = jasmine.createSpyObj('StudyDetailsResource', ['get']);
        StudyArmResource = jasmine.createSpyObj('StudyArmResource', ['query']);
        StudyEpochResource = jasmine.createSpyObj('StudyEpochResource', ['query']);

        StudyPopulationCharacteristicsResource = jasmine.createSpyObj('StudyEpochResource', ['get']);
        StudyEndpointsResource = jasmine.createSpyObj('StudyEpochResource', ['get']);
        StudyAdverseEventsResource = jasmine.createSpyObj('StudyEpochResource', ['get']);


        StudyTreatmentActivityResource = jasmine.createSpyObj('StudyTreatmentActivityResource', ['query']);
        StudyTreatmentActivityResource.query.and.returnValue(treatmentActivities);

        scope = {};

        ctrl = $controller('StudyController', {
          $scope: scope,
          $stateParams: {},
          TrialverseResource: TrialverseResource,
          StudyDetailsResource: StudyDetailsResource,
          StudyTreatmentActivityResource: StudyTreatmentActivityResource,
          StudyArmResource: StudyArmResource,
          StudyEpochResource: StudyEpochResource,
          StudyPopulationCharacteristicsResource: StudyPopulationCharacteristicsResource,
          StudyEndpointsResource: StudyEndpointsResource,
          StudyAdverseEventsResource: StudyAdverseEventsResource
        });

      }));

      describe("cellTreatments function", function() {

        it("should select the treatments that match the given epoch and arm", function() {
          var epochUid = 'epcohUid';
          var armUid = 'armUid';

          expect(scope.cellTreatments(epochUid, armUid)[0]).toBe(treatmentActivity);
        });
      });

    });
  });