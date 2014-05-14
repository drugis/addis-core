define(['angular', 'angular-mocks', 'services'], function() {
  describe("The networkMetaAnalysis service", function() {

    describe('transformTrialDataToTableRows', function() {

      beforeEach(module('addis.services'));

      it('should construct table rows from the list of studies',
        inject(function($rootScope, NetworkMetaAnalysisService) {
          var exampleStudies = [
            {"studyId":48,"title":"TAK491-301 / NCT00846365",
             "trialDataInterventions":[
               {"drugId":109,"uri":"http://trials.drugis.org/namespaces/2/drug/87fec8a8071915a2e17eddeb1faf8daa"},
               {"drugId":110,"uri":"http://trials.drugis.org/namespaces/2/drug/a977e3a6fa4dc0a34fcf9fb351bc0a0e"}
              ]
            },
            {"studyId":44,"title":"TAK491-008 / NCT00696241",
             "trialDataInterventions":[
               {"drugId":98,"uri":"http://trials.drugis.org/namespaces/2/drug/87fec8a8071915a2e17eddeb1faf8daa"}
             ]
            }
          ];

          var result = NetworkMetaAnalysisService.transformTrialDataToTableRows(exampleStudies);
          expect(result[0]).toEqual({study: "TAK491-301 / NCT00846365", rowSpan: 2, intervention: 109});
          expect(result[1]).toEqual({intervention: 110});
          expect(result[2]).toEqual({study: "TAK491-008 / NCT00696241", rowSpan: 1, intervention: 98});
        })
      );

    });

  });
});