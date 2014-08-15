define(['angular', 'angular-mocks', 'services'], function() {
  describe("The study design  service", function() {

    var treatmentActivities = [{
      "treatmentActivityUri": "http://trials.drugis.org/instances/bf8f4077-3143-43d9-a819-8a611de10f23",
      "epochUri": "http://trials.drugis.org/instances/4ebdb094-f736-4f1e-8fd1-12cadc79f9fc",
      "epochLabel": "Screening",
      "treatmentActivityTypeLabel": "ScreeningActivity",
      "armLabel": null,
      "treatmentDrugLabel": null,
      "minValue": null,
      "minUnitLabel": null,
      "minDosingPeriodicity": null,
      "maxValue": null,
      "maxUnitLabel": null,
      "maxDosingPeriodicity": null,
      "fixedValue": null,
      "fixedUnitLabel": null,
      "fixedDosingPeriodicity": null
    }, {
      "treatmentActivityUri": "http://trials.drugis.org/instances/1d589c10-ac70-44c2-89ad-894ecb515954",
      "epochUri": "http://trials.drugis.org/instances/5e3df3f4-f266-4cb9-abfc-155bbaf2fb33",
      "epochLabel": "Placebo run-in",
      "treatmentActivityTypeLabel": "WashOutActivity",
      "armLabel": null,
      "treatmentDrugLabel": null,
      "minValue": null,
      "minUnitLabel": null,
      "minDosingPeriodicity": null,
      "maxValue": null,
      "maxUnitLabel": null,
      "maxDosingPeriodicity": null,
      "fixedValue": null,
      "fixedUnitLabel": null,
      "fixedDosingPeriodicity": null
    }, {
      "treatmentActivityUri": "http://trials.drugis.org/instances/139fcfd8-8e41-497e-8068-711f92f134a8",
      "epochUri": "http://trials.drugis.org/instances/d94b770e-0004-4601-b325-c43ae24522d4",
      "epochLabel": "Randomization",
      "treatmentActivityTypeLabel": "RandomizationActivity",
      "armLabel": null,
      "treatmentDrugLabel": null,
      "minValue": null,
      "minUnitLabel": null,
      "minDosingPeriodicity": null,
      "maxValue": null,
      "maxUnitLabel": null,
      "maxDosingPeriodicity": null,
      "fixedValue": null,
      "fixedUnitLabel": null,
      "fixedDosingPeriodicity": null
    }, {
      "treatmentActivityUri": "http://trials.drugis.org/instances/bdac10f4-13ee-40b5-a4e4-61c2278743db",
      "epochUri": "http://trials.drugis.org/instances/d1fd52de-a0b8-428a-9896-b3792bf42292",
      "epochLabel": "Main phase",
      "treatmentActivityTypeLabel": "TreatmentActivity",
      "armLabel": "Fluoxetine",
      "treatmentDrugLabel": "Fluoxetine",
      "minValue": 20.0,
      "minUnitLabel": "milligram",
      "minDosingPeriodicity": "P1D",
      "maxValue": 80.0,
      "maxUnitLabel": "milligram",
      "maxDosingPeriodicity": "P1D",
      "fixedValue": null,
      "fixedUnitLabel": null,
      "fixedDosingPeriodicity": null
    }, {
      "treatmentActivityUri": "http://trials.drugis.org/instances/461e1fe8-5882-4755-8087-954f9c475cab",
      "epochUri": "http://trials.drugis.org/instances/d1fd52de-a0b8-428a-9896-b3792bf42292",
      "epochLabel": "Main phase",
      "treatmentActivityTypeLabel": "TreatmentActivity",
      "armLabel": "Paroxetine",
      "treatmentDrugLabel": "Paroxetine",
      "minValue": 20.0,
      "minUnitLabel": "milligram",
      "minDosingPeriodicity": "P1D",
      "maxValue": 50.0,
      "maxUnitLabel": "milligram",
      "maxDosingPeriodicity": "P1D",
      "fixedValue": null,
      "fixedUnitLabel": null,
      "fixedDosingPeriodicity": null
    }];
    beforeEach(module('addis.services'));

    beforeEach(inject(function(StudyDesignService) {
      this.StudyDesignService = StudyDesignService;
    }));

    it('build a studyDesign table based in a list of treatmentActivities', function() {
      var table = this.StudyDesignService.buildStudyDesignTable(treatmentActivities);
      expect(table.head).toEqual(['Arms', 'N', 'Screening', 'Placebo run-in', 'Randomization', 'Main phase']);
      expect(table.body.length).toEqual(2);
      expect(table.body[0]).toEqual([{
        label: 'Fluoxetine'
      }, {
        numberOfParticipantsStarting: undefined
      }, , {
        label: 'ScreeningActivity'
      }, {
        label: 'WashOutActivity'
      }, {
        label: 'RandomizationActivity'
      }, {
        label: 'Fluoxetine',
        fixedDosingPeriodicity: null,
        fixedUnitLabel: null,
        fixedValue: null,
        maxDosingPeriodicity: 'P1D',
        maxUnitLabel: 'milligram',
        maxValue: 80,
        minDosingPeriodicity: 'P1D',
        minUnitLabel: 'milligram',
        minValue: 20
      }]);
    });

  });
});