define(['angular', 'angular-mocks', 'services'], function() {

  var exampleStudies = [{
    "studyId": 48,
    "name": "TAK491-301 / NCT00846365",
    "trialDataInterventions": [{
      "drugId": 109,
      "uri": "http://trials.drugis.org/namespaces/2/drug/87fec8a8071915a2e17eddeb1faf8daa"
    }, {
      "drugId": 110,
      "uri": "http://trials.drugis.org/namespaces/2/drug/a977e3a6fa4dc0a34fcf9fb351bc0a0e"
    }]
  }, {
    "studyId": 44,
    "name": "TAK491-008 / NCT00696241",
    "trialDataInterventions": [{
      "drugId": 98,
      "uri": "http://trials.drugis.org/namespaces/2/drug/87fec8a8071915a2e17eddeb1faf8daa"
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
            semanticInterventionUri: 'http://trials.drugis.org/namespaces/2/drug/87fec8a8071915a2e17eddeb1faf8daa'
          }, {
            name: 'intervention2',
            semanticInterventionUri: 'http://trials.drugis.org/namespaces/2/drug/a977e3a6fa4dc0a34fcf9fb351bc0a0e'
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
            study: "TAK491-301 / NCT00846365",
            rowSpan: 2,
            intervention: 'intervention1'
          });
          expect(expectedRows[1]).toEqual({
            intervention: 'intervention2'
          });
          expect(expectedRows[2]).toEqual({
            study: "TAK491-008 / NCT00696241",
            rowSpan: 1,
            intervention: 'intervention1'
          });
        })
      );

    });

  });
});