'use strict';
define(['angular', 'angular-mocks', 'controllers'],
  function() {
    describe("The study readOnly controller", function() {
      var ctrl, TrialverseResource, StudyDetailsResource,
        StudyTreatmentActivityResource, StudyGroupResource, StudyEpochResource,
        StudyPopulationCharacteristicsResource, StudyEndpointsResource, StudyAdverseEventsResource,
        treatmentActivity;

      beforeEach(module('addis.controllers'));

      beforeEach(inject(function($controller, $q) {
        treatmentActivity = {
          activityApplications: [{
            epochUid: 'epcohUid',
            armUid: 'armUid'
          }]
        };
        var treatmentActivities = [treatmentActivity];

        TrialverseResource = jasmine.createSpyObj('TrialverseResource', ['get']);
        StudyDetailsResource = jasmine.createSpyObj('StudyDetailsResource', ['get']);
        StudyGroupResource = jasmine.createSpyObj('StudyGroupResource', ['query']);
        StudyEpochResource = jasmine.createSpyObj('StudyEpochResource', ['query']);

        StudyPopulationCharacteristicsResource = jasmine.createSpyObj('StudyEpochResource', ['get']);
        StudyEndpointsResource = jasmine.createSpyObj('StudyEpochResource', ['get']);
        StudyAdverseEventsResource = jasmine.createSpyObj('StudyEpochResource', ['get']);


        StudyTreatmentActivityResource = jasmine.createSpyObj('StudyTreatmentActivityResource', ['query']);
        StudyTreatmentActivityResource.query.and.returnValue(treatmentActivities);

        ctrl = $controller('StudyController', {

          $q: $q,
          $stateParams: {},
          TrialverseResource: TrialverseResource,
          StudyDetailsResource: StudyDetailsResource,
          StudyTreatmentActivityResource: StudyTreatmentActivityResource,
          StudyGroupResource: StudyGroupResource,
          StudyEpochResource: StudyEpochResource,
          StudyPopulationCharacteristicsResource: StudyPopulationCharacteristicsResource,
          StudyEndpointsResource: StudyEndpointsResource,
          StudyAdverseEventsResource: StudyAdverseEventsResource
        });

      }));

    });
  });
