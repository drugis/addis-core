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

  describe("The networkMetaAnalysisService", function() {

    describe('transformTrialDataToTableRows', function() {

      beforeEach(module('addis.services'));
      beforeEach(module('addis.resources'));

      beforeEach(function() {

        mockInterventionResource = jasmine.createSpyObj('InterventionResource', ['query']);

        module('addis', function($provide) {
          $provide.value('InterventionResource', mockInterventionResource);
        });
      });

      it('should construct table rows from the list of trialDataStudies',
        inject(function($rootScope, $q, NetworkMetaAnalysisService) {

          var trialVersStudyData = {};
          trialVersStudyData.trialDataStudies = exampleStudies;
          var interventionsDefer = $q.defer();
          var interventions = [{
            name: 'intervention1',
            semanticInterventionUri: 'http://trials.drugis.org/namespaces/1/drug/a4b119795fa42c624640a77ce024d9a2'
          }, {
            name: 'intervention2',
            semanticInterventionUri: 'http://trials.drugis.org/namespaces/1/drug/a0f638328eeea353bf0ba7f111a167dd'
          }, {
            name: 'intervention3',
            semanticInterventionUri: 'http://trials.drugis.org/namespaces/2/drug/87fec8a8071915a2e17eddeb1faf8daa'
          }];
          interventions.$promise = interventionsDefer.promise;
          mockInterventionResource.query.and.returnValue(interventions);


          // Execute
          var resultPromise = NetworkMetaAnalysisService.transformTrialDataToTableRows(trialVersStudyData);

          // Expect promise
          expect(resultPromise.then).not.toBeNull();
          var expectedRows;
          resultPromise.then(function(result) {
            expectedRows = result;
          });
          interventionsDefer.resolve(interventions);
          $rootScope.$apply();

          expect(expectedRows[0]).toEqual({
            study: "Fava et al, 2002",
            studyRowSpan: 3,
            intervention: 'intervention1',
            arm : 'Paroxetine',
            rate : 5,
            sampleSize : 96 
          });
          expect(expectedRows[1]).toEqual({
            arm: "Sertraline",
            intervention: "intervention1",
            rate: 1,
            sampleSize: 96,
          });
          expect(expectedRows[2]).toEqual({
            arm: "Fluoxetine",
            intervention: "intervention2",
            rate: 1,
            sampleSize: 92
          });
        })
      );

    });

  });
});