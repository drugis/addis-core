define(['angular', 'angular-mocks', 'services'], function() {

    var exampleStudies = [{
      "studyId": 27,
      "name": "Fava et al, 2002",
      "trialDataInterventions": [{
        "drugId": 58,
        "uri": "http://trials.drugis.org/namespaces/1/drug/a4b119795fa42c624640a77ce024d9a2",
        "studyId": 27
      }, {
        "drugId": 60,
        "uri": "http://trials.drugis.org/namespaces/1/drug/a0f638328eeea353bf0ba7f111a167dd",
        "studyId": 27
      }],
      "trialDataArms": [{
        "id": 86,
        "name": "Paroxetine",
        "study": 27,
        "drugId": 58,
        "measurements": [{
          "integerValue": 5,
          "realValue": null,
          "measurementMomentId": 53,
          "studyId": 27,
          "armId": 86,
          "variableId": 428,
          "measurementAttribute": "rate"
        }, {
          "integerValue": 96,
          "realValue": null,
          "measurementMomentId": 53,
          "studyId": 27,
          "armId": 86,
          "variableId": 428,
          "measurementAttribute": "sample size"
        }]
      }, {
        "id": 85,
        "name": "Sertraline",
        "study": 27,
        "drugId": 58,
        "measurements": [{
          "integerValue": 1,
          "realValue": null,
          "measurementMomentId": 53,
          "studyId": 27,
          "armId": 85,
          "variableId": 428,
          "measurementAttribute": "rate"
        }, {
          "integerValue": 96,
          "realValue": null,
          "measurementMomentId": 53,
          "studyId": 27,
          "armId": 85,
          "variableId": 428,
          "measurementAttribute": "sample size"
        }]
      }, {
        "id": 87,
        "name": "Fluoxetine",
        "study": 27,
        "drugId": 60,
        "measurements": [{
          "integerValue": 1,
          "realValue": null,
          "measurementMomentId": 53,
          "studyId": 27,
          "armId": 87,
          "variableId": 428,
          "measurementAttribute": "rate"
        }, {
          "integerValue": 92,
          "realValue": null,
          "measurementMomentId": 53,
          "studyId": 27,
          "armId": 87,
          "variableId": 428,
          "measurementAttribute": "sample size"
        }]
      }]
    }];



    function exampleInterventions() {
      return [{
        name: 'intervention 1',
        semanticInterventionUri: 'http://trials.drugis.org/namespaces/1/drug/a4b119795fa42c624640a77ce024d9a2'
      }, {
        name: 'intervention 2',
        semanticInterventionUri: 'http://trials.drugis.org/namespaces/1/drug/a0f638328eeea353bf0ba7f111a167dd'
      }, {
        name: 'intervention 3',
        semanticInterventionUri: 'http://trials.drugis.org/namespaces/2/drug/87fec8a8071915a2e17eddeb1faf8daa'}];
    }

    describe("The networkMetaAnalysisService", function() {

        describe('transformTrialDataToNetwork', function() {
            beforeEach(module('addis.services'));

            it('should construct the evidence network from the list of trialDataStudies', inject(function(NetworkMetaAnalysisService) {
              var trialVersStudyData = {};
              trialVersStudyData.trialDataStudies = exampleStudies;
              var interventions = exampleInterventions();

              var network = NetworkMetaAnalysisService.transformTrialDataToNetwork(trialVersStudyData, interventions);

              expect(network).toEqual(exampleNetwork);
            }));

        });

      describe('transformTrialDataToTableRows', function() {

        beforeEach(module('addis.services'));

        it('should construct table rows from the list of trialDataStudies',
          inject(function($rootScope, $q, NetworkMetaAnalysisService) {

            var trialVersStudyData = {};
            trialVersStudyData.trialDataStudies = exampleStudies;
            var interventions = exampleInterventions();

            // Execute
            var resultRows = NetworkMetaAnalysisService.transformTrialDataToTableRows(trialVersStudyData, interventions);

            expect(resultRows[0]).toEqual({
              study: 'Fava et al, 2002',
              studyRowSpan: 3,
              intervention: 'intervention 1',
              arm: 'Paroxetine',
              rate: 5,
              mu: null,
              sigma: null,
              sampleSize: 96,
              firstInterventionRow: true,
              firstStudyRow: true,
              interventionRowSpan: 2
            });

            expect(resultRows[1]).toEqual({
              arm: 'Sertraline',
              intervention: 'intervention 1',
              mu: null,
              rate: 1,
              sampleSize: 96,
              sigma: null,
              study: 'Fava et al, 2002',
              studyRowSpan: 3
            });

            expect(resultRows[2]).toEqual({
              arm: 'Fluoxetine',
              firstInterventionRow: true,
              intervention: 'intervention 2',
              interventionRowSpan: 1,
              mu: null,
              rate: 1,
              sampleSize: 92,
              sigma: null,
              study: 'Fava et al, 2002',
              studyRowSpan: 3
            });
          })
        );

      });

    });
});