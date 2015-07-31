define(['angular', 'angular-mocks', 'services'], function() {

  var exampleStudies = [{
    "studyUid": "27",
    "name": "Fava et al, 2002",
    "trialDataInterventions": [{
      "drugInstanceUid": "58",
      "drugConceptUid": "http://trials.drugis.org/namespaces/1/drug/a4b119795fa42c624640a77ce024d9a2",
      "studyUid": "27"
    }, {
      "drugInstanceUid": "60",
      "drugConceptUid": "http://trials.drugis.org/namespaces/1/drug/a0f638328eeea353bf0ba7f111a167dd",
      "studyUid": "27"
    }],
    "trialDataArms": [{
      "uid": "86",
      "name": "Paroxetine",
      "studyUid": "27",
      "drugInstanceUid": "58",
      "drugConceptUid": "http://trials.drugis.org/namespaces/1/drug/a4b119795fa42c624640a77ce024d9a2",
      "measurement": {
        "studyUid": "27",
        "mean": null,
        "rate": 5,
        "sampleSize": 96,
        "stdDev": null,
        "armUid": "86",
        "variableUid": "428"
      }
    }, {
      "uid": "85",
      "name": "Sertraline",
      "studyUid": "27",
      "drugInstanceUid": "58",
      "drugConceptUid": "http://trials.drugis.org/namespaces/1/drug/a4b119795fa42c624640a77ce024d9a2",
      "measurement": {
        "studyUid": "27",
        "mean": null,
        "rate": 1,
        "sampleSize": 96,
        "stdDev": null,
        "armUid": "86",
        "variableUid": "428"
      }
    }, {
      "uid": "87",
      "name": "Fluoxetine",
      "studyUid": "27",
      "drugInstanceUid": "60",
      "drugConceptUid": "http://trials.drugis.org/namespaces/1/drug/a0f638328eeea353bf0ba7f111a167dd",
      "measurement": {
        "rate": 1,
        "mean": null,
        "stdDev": null,
        "studyUid": "27",
        "armUid": "87",
        "variableUid": "428",
        "sampleSize": 92
      }
    }]
  }];

  var exampleNetworkStudies = [{
    "studyUid": "44",
    "name": "TAK491-008 / NCT00696241",
    "trialDataInterventions": [{
      "drugInstanceUid": "100",
      "drugConceptUid": "http://trials.drugis.org/namespaces/2/drug/6f8ce038bc50a5372fcaf86e4b300bb6",
      "studyUid": "44"
    }, {
      "drugInstanceUid": "99",
      "drugConceptUid": "http://trials.drugis.org/namespaces/2/drug/d7d7c477b89a5e4e5a57318743ccc87e",
      "studyUid": "44"
    }],
    "trialDataArms": [{
      "uid": "144",
      "name": "Azilsartan Medoxomil 40 mg QD",
      "studyUid": "44",
      "drugInstanceUid": "98",
      "measurement": {
        "mean": -14.47,
        "armUid": "144",
        "variableUid": 698,
        "measurementAttribute": "mean",
        "studyUid": 44,
        "sampleSize": 276,
        "stdDev": 15.815811834996014
      }
    }, {
      "uid": "145",
      "name": "Azilsartan Medoxomil 80 mg QD",
      "study": "44",
      "drugInstanceUid": "98",
      "measurement": {
        "mean": -17.58,
        "armUid": "145",
        "variableUid": "698",
        "studyUid": "44",
        "sampleSize": 279,
        "stdDev": 15.818018554800092
      }
    }, {
      "uid": "146",
      "name": "Olmesartan 40 mg QD",
      "studyUid": "44",
      "drugInstanceUid": "99",
      "drugConceptUid": "http://trials.drugis.org/namespaces/2/drug/d7d7c477b89a5e4e5a57318743ccc87e",
      "measurement": {
        "mean": -14.87,
        "armUid": "146",
        "variableUid": "698",
        "studyUid": 44,
        "sampleSize": 280,
        "stdDev": 15.812874501494028
      }
    }, {
      "uid": "147",
      "name": "Placebo QD",
      "studyUid": "44",
      "drugInstanceUid": "100",
      "drugConceptUid": "http://trials.drugis.org/namespaces/2/drug/6f8ce038bc50a5372fcaf86e4b300bb6",
      "measurement": {
        "mean": -2.06,
        "armUid": "147",
        "variableUid": "698",
        "studyUid": "44",
        "sampleSize": 140,
        "stdDev": 15.819597340008373
      }
    }, {
      "uid": 143,
      "name": "Azilsartan Medoxomil 20 mg QD",
      "studyUid": "44",
      "drugInstanceUid": "98",
      "measurement": {
        "mean": -14.28,
        "armUid": "143",
        "variableUid": 698,
        "studyUid": "44",
        "sampleSize": 274,
        "stdDev": 15.824615761527987
      }
    }]
  }, {
    "studyUid": "45",
    "name": "TAK491-011 / NCT00591253",
    "trialDataInterventions": [{
      "drugInstanceUid": "102",
      "drugConceptUid": "http://trials.drugis.org/namespaces/2/drug/6f8ce038bc50a5372fcaf86e4b300bb6",
      "studyUid": "45"
    }],
    "trialDataArms": [{
      "uid": "149",
      "name": "Azilsartan Medoxomil 40 mg QD",
      "studyUid": "45",
      "drugInstanceUid": "101",
      "measurement": {
        "mean": -9.51,
        "armUid": "149",
        "variableUid": "731",
        "measurementAttribute": "mean",
        "studyUid": "45",
        "sampleSize": 134,
        "stdDev": 15.395863080711
      }
    }, {
      "uid": "150",
      "name": "Azilsartan Medoxomil 80 mg QD",
      "studyUid": "45",
      "drugInstanceUid": "101",
      "measurement": {
        "mean": -9.58,
        "armUid": "150",
        "variableUid": "731",
        "measurementAttribute": "mean",
        "studyUid": "45",
        "sampleSize": 130,
        "stdDev": 15.403769993089353
      }
    }, {
      "uid": "151",
      "name": "Placebo QD",
      "studyUid": "45",
      "drugInstanceUid": "102",
      "drugConceptUid": "http://trials.drugis.org/namespaces/2/drug/6f8ce038bc50a5372fcaf86e4b300bb6",
      "measurement": {
        "mean": -3.04,
        "armUid": 151,
        "variableUid": 731,
        "studyUid": "45",
        "sampleSize": 271,
        "stdDev": 15.37290593869617
      }
    }]
  }, {
    "studyUid": "46",
    "name": "TAK491-019 / NCT00696436",
    "trialDataInterventions": [{
      "drugInstanceUid": "106",
      "drugConceptUid": "http://trials.drugis.org/namespaces/2/drug/6f8ce038bc50a5372fcaf86e4b300bb6",
      "studyUid": "46"
    }, {
      "drugInstanceUid": "105",
      "drugConceptUid": "http://trials.drugis.org/namespaces/2/drug/d7d7c477b89a5e4e5a57318743ccc87e",
      "studyUid": "46"
    }],
    "trialDataArms": [{
      "uid": "156",
      "name": "Olmesartan 40 mg QD",
      "studyUid": "46",
      "drugInstanceUid": "105",
      "drugConceptUid": "http://trials.drugis.org/namespaces/2/drug/d7d7c477b89a5e4e5a57318743ccc87e",
      "measurement": {
        "mean": -13.2,
        "armUid": "156",
        "variableUid": "758",
        "studyUid": "46",
        "sampleSize": 283,
        "stdDev": 15.729134591578775
      }
    }, {
      "uid": 155,
      "name": "Valsartan 320 mg QD",
      "studyUid": "46",
      "drugInstanceUid": "104",
      "measurement": {
        "mean": -11.31,
        "armUid": "155",
        "variableUid": "758",
        "studyUid": "46",
        "sampleSize": 271,
        "stdDev": 15.721284139662384
      }
    }, {
      "uid": 157,
      "name": "Placebo QD",
      "studyUid": "46",
      "drugInstanceUid": "106",
      "drugConceptUid": "http://trials.drugis.org/namespaces/2/drug/6f8ce038bc50a5372fcaf86e4b300bb6",
      "measurement": {
        "mean": -1.83,
        "armUid": "157",
        "variableUid": "758",
        "studyUid": "46",
        "sampleSize": 148,
        "stdDev": 15.730023903351194
      }
    }, {
      "uid": 154,
      "name": "Azilsartan Medoxomil 80 mg QD",
      "studyUid": "46",
      "drugInstanceUid": "103",
      "measurement": {
        "realValue": -16.74,
        "armUid": 154,
        "variableUid": 758,
        "studyUid": "46",
        "sampleSize": 270,
        "stdDev": 15.725114625973317
      }
    }, {
      "uid": "153",
      "name": "Azilsartan Medoxomil 40 mg QD",
      "studyUid": "46",
      "drugInstanceUid": "103",
      "measurement": {
        "mean": -16.38,
        "armUid": "153",
        "variableUid": "758",
        "studyUid": "46",
        "sampleSize": 269,
        "stdDev": 15.728769468715601
      }
    }]
  }];

  var expectedNetwork = {
    "interventions": [{
      "name": "placebo",
      "sampleSize": 288
    }, {
      "name": "Olmes",
      "sampleSize": 563
    }, {
      "name": "Chlora",
      "sampleSize": 0
    }],
    "edges": [{
      "from": {
        "id": 43,
        "project": 13,
        "name": "placebo",
        "motivation": "",
        "semanticInterventionLabel": "Placebo",
        "semanticInterventionUri": "http://trials.drugis.org/namespaces/2/drug/6f8ce038bc50a5372fcaf86e4b300bb6"
      },
      "to": {
        "id": 44,
        "project": 13,
        "name": "Olmes",
        "motivation": "",
        "semanticInterventionLabel": "Olmesartan",
        "semanticInterventionUri": "http://trials.drugis.org/namespaces/2/drug/d7d7c477b89a5e4e5a57318743ccc87e"
      },
      "studies" : [exampleNetworkStudies[0], exampleNetworkStudies[2]]
    }]
  };

  function exampleInterventions() {
    return [{
      semanticInterventionLabel: 'intervention 1',
      semanticInterventionUri: 'http://trials.drugis.org/namespaces/1/drug/a4b119795fa42c624640a77ce024d9a2'
    }, {
      semanticInterventionLabel: 'intervention 2',
      semanticInterventionUri: 'http://trials.drugis.org/namespaces/1/drug/a0f638328eeea353bf0ba7f111a167dd'
    }, {
      semanticInterventionLabel: 'intervention 3',
      semanticInterventionUri: 'http://trials.drugis.org/namespaces/2/drug/87fec8a8071915a2e17eddeb1faf8daa'
    }];
  }

  var networkInterventions = [{
    "id": 43,
    "project": 13,
    "name": "placebo",
    "motivation": "",
    "semanticInterventionLabel": "Placebo",
    "semanticInterventionUri": "http://trials.drugis.org/namespaces/2/drug/6f8ce038bc50a5372fcaf86e4b300bb6"
  }, {
    "id": 44,
    "project": 13,
    "name": "Olmes",
    "motivation": "",
    "semanticInterventionLabel": "Olmesartan",
    "semanticInterventionUri": "http://trials.drugis.org/namespaces/2/drug/d7d7c477b89a5e4e5a57318743ccc87e"
  }, {
    "id": 45,
    "project": 13,
    "name": "Chlora",
    "motivation": "",
    "semanticInterventionLabel": "Chlortalidone",
    "semanticInterventionUri": "http://trials.drugis.org/namespaces/2/drug/a977e3a6fa4dc0a34fcf9fb351bc0a0e"
  }];

  describe("The networkMetaAnalysisService", function() {

    describe('transformTrialDataToNetwork', function() {
      beforeEach(module('addis.services'));

      it('should construct the evidence network from the list of trialDataStudies', inject(function(NetworkMetaAnalysisService) {
        var trialVerseStudyData = {
          trialDataStudies: exampleNetworkStudies
        };

        var network = NetworkMetaAnalysisService.transformTrialDataToNetwork(trialVerseStudyData, networkInterventions);

        expect(network).toEqual(expectedNetwork);
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
            studyUid: "27",
            studyRowSpan: 3,
            studyRows: resultRows,
            intervention: 'intervention 1',
            arm: 'Paroxetine',
            rate: 5,
            mu: null,
            sigma: null,
            sampleSize: 96,
            firstInterventionRow: true,
            firstStudyRow: true,
            interventionRowSpan: 2,
            included: true,
            drugInstanceUid: "58",
            trialverseUid: "86",
            drugConceptUid: "http://trials.drugis.org/namespaces/1/drug/a4b119795fa42c624640a77ce024d9a2"
          });

          expect(resultRows[1]).toEqual({
            arm: 'Sertraline',
            intervention: 'intervention 1',
            mu: null,
            rate: 1,
            sampleSize: 96,
            sigma: null,
            study: 'Fava et al, 2002',
            studyUid: "27",
            studyRowSpan: 3,
            studyRows: resultRows,
            included: true,
            drugInstanceUid: "58",
            trialverseUid: "85",
            drugConceptUid: "http://trials.drugis.org/namespaces/1/drug/a4b119795fa42c624640a77ce024d9a2"
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
            studyUid: "27",
            studyRowSpan: 3,
            studyRows: resultRows,
            included: true,
            drugInstanceUid: "60",
            trialverseUid: "87",
            drugConceptUid: "http://trials.drugis.org/namespaces/1/drug/a0f638328eeea353bf0ba7f111a167dd"
          });
        })
      );

    });

    describe('isNetworkDisconnected', function() {

      beforeEach(module('addis.services'));

      it('should return true if the network is connected', inject(function(NetworkMetaAnalysisService) {
        var network = {
          interventions: [{
            name: 'A'
          }, {
            name: 'B'
          }, {
            name: 'C'
          }],
          edges: [{
            from: {
              name: 'A'
            },
            to: {
              name: 'B'
            }
          }, {
            from: {
              name: 'B'
            },
            to: {
              name: 'C'
            }
          }]
        };

        expect(NetworkMetaAnalysisService.isNetworkDisconnected(network)).toBeFalsy();

        network.edges.pop();

        expect(NetworkMetaAnalysisService.isNetworkDisconnected(network)).toBeTruthy();
      }));

      it('should return false for a network that has two connected subnetworks', inject(function(NetworkMetaAnalysisService) {
        var network = {
          interventions: [{
            name: 'A'
          }, {
            name: 'B'
          }, {
            name: 'C'
          }, {
            name: 'D'
          }],
          edges: [{
            from: {
              name: 'A'
            },
            to: {
              name: 'B'
            }
          }, {
            from: {
              name: 'C'
            },
            to: {
              name: 'D'
            }
          }]
        };
        expect(NetworkMetaAnalysisService.isNetworkDisconnected(network)).toBeTruthy();
      }));
    });

    describe('changeArmExclusion', function() {

      beforeEach(module('addis.services'));

      it('should add an excluded arm if dataRow.included is false', inject(function(NetworkMetaAnalysisService) {
        var dataRow = {
          included: false,
          trialverseId: -1
        };

        var analysis = {
          id: 1,
          excludedArms: [{
            trialverseId: -2,
            analysisId: 1
          }],
        };

        analysis = NetworkMetaAnalysisService.changeArmExclusion(dataRow, analysis);
        expect(analysis.excludedArms.length).toBe(2);
      }));

      it('should remove an excluded arm if datarow.included = true for the corresponding arm', inject(function(NetworkMetaAnalysisService) {
        var dataRow = {
          included: true,
          trialverseId: -2
        };

        var analysis = {
          id: 1,
          excludedArms: [{
            trialverseId: -2,
            analysisId: 1
          }, {
            trialverseId: -3,
            analysisId: 1
          }],
        };
        analysis = NetworkMetaAnalysisService.changeArmExclusion(dataRow, analysis);
        expect(analysis.excludedArms.length).toBe(1);
        expect(analysis.excludedArms[0].trialverseId).toBe(-3);
      }));

    });

    describe('doesInterventionHaveAmbiguousArms', function() {
      beforeEach(module('addis.services'));

      it('should return true if there are ambiguous arms for the intervention', inject(function(NetworkMetaAnalysisService) {
        var drugConceptUid = "1";
        var studyUid = "27";
        var trialverseData = {
          trialDataStudies: [{
            studyUid : studyUid,
            trialDataArms: [{
              drugConceptUid: "1"
            }, {
              drugConceptUid: "1"
            }],
            trialDataInterventions: [{
              drugConceptUid: "1"
            }],
          }]
        };
        var analysis = {
          excludedArms: []
        };
        expect(NetworkMetaAnalysisService.doesInterventionHaveAmbiguousArms(drugConceptUid, studyUid, trialverseData, analysis)).toBeTruthy();
      }));

      it('should return false if there are no ambiguous arms for the intervention', inject(function(NetworkMetaAnalysisService) {
        var drugConceptUid = 1;
        var studyUid = "27";
        var trialverseData = {
          trialDataStudies: [{
            studyUid : studyUid,
            trialDataArms: [{
              drugConceptUid: 1
            }, {
              drugConceptUid: 2
            }]
          }]
        };
        var analysis = {
          excludedArms: []
        };
        expect(NetworkMetaAnalysisService.doesInterventionHaveAmbiguousArms(drugConceptUid, studyUid, trialverseData, analysis)).toBeFalsy();
      }));

      it('should return false if the ambiguity has been resolved through exclusion', inject(function(NetworkMetaAnalysisService) {
        var drugConceptUid = 1;
        var studyUid = "27";
        var trialverseData = {
          trialDataStudies: [{
            studyUid : studyUid,
            trialDataArms: [{
              id: 3,
              drugConceptUid: 1
            }, {
              id: 4,
              drugConceptUid: 1
            }]
          }]
        };
        var analysis = {
          excludedArms: [{
            trialverseId: 3
          }]
        };
        expect(NetworkMetaAnalysisService.doesInterventionHaveAmbiguousArms(drugConceptUid, studyUid, trialverseData, analysis)).toBeFalsy();
      }));

    });

   describe('doesModelHaveAmbiguousArms', function() {
      beforeEach(module('addis.services'));

      it('should return true if there are ambiguous arms for the model', inject(function(NetworkMetaAnalysisService) {
        var interventions = [{semanticInterventionUri: "uri1", isIncluded: true}];
        var trialverseData = {
          trialDataStudies: [{
            trialDataArms: [{
              drugInstanceUid: 1,
              drugConceptUid: "uri1"
            }, {
              drugInstanceUid: 1,
              drugConceptUid: "uri1"
            }],
            trialDataInterventions: [{
              drugInstanceUid: 1,
              drugConceptUid: "uri1"
            }],
          }]
        };
        var analysis = {
          excludedArms: []
        };
        expect(NetworkMetaAnalysisService.doesModelHaveAmbiguousArms(trialverseData, interventions, analysis)).toBeTruthy();
      }));

    });

    describe('addInclusionsToInterventions', function() {
      beforeEach(module('addis.services'));

      it('should add inclusions to interventions', inject(function(NetworkMetaAnalysisService) {
        var inclusions = [{
          interventionId: 43
        }];
        var includedInterventions = NetworkMetaAnalysisService.addInclusionsToInterventions(networkInterventions, inclusions);
        expect(includedInterventions[0].isIncluded).toBeTruthy();
        expect(includedInterventions[1].isIncluded).toBeFalsy();
        expect(includedInterventions[2].isIncluded).toBeFalsy();
      }));
    });

    describe('buildInterventionInclusions', function() {
      beforeEach(module('addis.services'));

      it('should create a new list of intervention inclusions', inject(function(NetworkMetaAnalysisService) {
        var interventions = [{
          id: 1,
          isIncluded: true
        }, {
          id: 2,
          isIncluded: false
        }, {
          id: 3,
          isIncluded: true
        }];
        var analysis = {
          id: 4
        };
        var interventionExclusions = NetworkMetaAnalysisService.buildInterventionInclusions(interventions, analysis);
        expect(interventionExclusions).toEqual([{
          interventionId: 1
        },{
          interventionId: 3
        }]);
      }));
    });

    describe('cleanUpExcludedArms', function() {
      beforeEach(module('addis.services'));

      it('should remove armExclusions that match the intervention', inject(function(NetworkMetaAnalysisService) {
        var study1 = {
          trialDataInterventions: [{
            drugInstanceUid: 1,
            drugConceptUid: "uri1"
          }, {
            drugInstanceUid: 2,
            drugConceptUid: "uri2"
          }, ],
          trialDataArms: [{
            drugInstanceUid: 1,
            drugConceptUid: "uri1",
            id: 10
          }, {
            drugInstanceUid: 2,
            drugConceptUid: "uri2",
            id: 11
          }, {
            drugInstanceUid: 2,
            drugConceptUid: "uri2",
            id: 12
          }]
        };
        var study2 = {
          trialDataInterventions: [{
            drugInstanceUid: 3,
            drugConceptUid: "uri1"
          }, {
            drugInstanceUid: 4,
            drugConceptUid: "uri3"
          }],
          trialDataArms: [{
            drugInstanceUid: 7,
            drugConceptUid: "uri1",
            id: 20
          }, {
            drugInstanceUid: 3,
            drugConceptUid: "uri3",
            id: 21
          }]
        };
        var trialverseData = {
          trialDataStudies: [study1, study2]
        };
        var analysis = {
          excludedArms: [{
            trialverseUid: 10
          }, {
            trialverseUid: 21
          }, {
            trialverseUid: 20
          }]
        };
        var intervention = {
          semanticInterventionUri: "uri1"
        };

        var expectedArmExclusions = [{
          trialverseUid: 21
        }];

        expect(NetworkMetaAnalysisService.cleanUpExcludedArms(intervention, analysis, trialverseData)).toEqual(expectedArmExclusions);

      }));
    });

  });
});
