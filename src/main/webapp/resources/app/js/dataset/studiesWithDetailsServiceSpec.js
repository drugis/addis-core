'use strict';
define(['angular-mocks'], function() {
  describe('the study with details service', function() {

    var studiesWithDetailsService;

    beforeEach(angular.mock.module('trialverse.dataset'));

    beforeEach(inject(function($window, StudiesWithDetailsService) {
      studiesWithDetailsService = StudiesWithDetailsService;
    }));

    describe('addActivitiesToStudies', function() {
      it('should add the treatments to the studies as human-readable strings (sorted alphabetically)', function() {
        var study1 = {
          studyUri: 'http://studies/1'
        };
        var study2 = {
          studyUri: 'http://studies/2'
        };
        var studies = [study1, study2];
        var activities = [{
          study: study1.studyUri,
          activity: 'activity1',
          drugName: 'xAzilsartan',
          treatmentType: 'http://trials.drugis.org/ontology#FixedDoseDrugTreatment',
          fixedDoseValue: '2.000000e+01',
          fixedDoseDosingPeriodicity: 'P1D',
          fixedDoseUnitLabel: 'mg'
        }, {
          study: study1.studyUri,
          activity: 'activity2',
          drugName: 'Paroxetine',
          treatmentType: 'http://trials.drugis.org/ontology#FixedDoseDrugTreatment',
          fixedDoseValue: '3.000000e+01',
          fixedDoseDosingPeriodicity: 'P1D',
          fixedDoseUnitLabel: 'mg'
        }, {
          study: study1.studyUri,
          activity: 'activity2',
          drugName: 'Metformine',
          treatmentType: 'http://trials.drugis.org/ontology#TitratedDoseDrugTreatment',
          minDoseValue: '1.00000e+01',
          minDoseDosingPeriodicity: 'P1D',
          minDoseUnitLabel: 'mg',
          maxDoseValue: '3.000000e+01',
          maxDoseDosingPeriodicity: 'P1D',
          maxDoseUnitLabel: 'mg'
        }, {
          study: study1.studyUri,
          activity: 'activity3',
          drugName: 'Parafine'
        }];

        var expectedResult = [{
          studyUri: 'http://studies/1',
          treatments: '<Activity unavailable: missing concept mapping>, Paroxetine 30 mg per 1 day(s) + Metformine 10-30 mg per 1 day(s), xAzilsartan 20 mg per 1 day(s)'
        }, {
          studyUri: 'http://studies/2',
          treatments: ''
        }];

        var result = studiesWithDetailsService.addActivitiesToStudies(studies, activities);

        expect(result).toEqual(expectedResult);
      });
    });
  });
});
