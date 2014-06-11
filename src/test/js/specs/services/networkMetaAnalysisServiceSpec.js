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

  var exampleNetworkStudies = [{
    "studyId": 44,
    "name": "TAK491-008 / NCT00696241",
    "trialDataInterventions": [{
      "drugId": 100,
      "uri": "http://trials.drugis.org/namespaces/2/drug/6f8ce038bc50a5372fcaf86e4b300bb6",
      "studyId": 44
    }, {
      "drugId": 99,
      "uri": "http://trials.drugis.org/namespaces/2/drug/d7d7c477b89a5e4e5a57318743ccc87e",
      "studyId": 44
    }],
    "trialDataArms": [{
      "id": 144,
      "name": "Azilsartan Medoxomil 40 mg QD",
      "study": 44,
      "drugId": 98,
      "measurements": [{
        "integerValue": null,
        "realValue": -14.47,
        "measurementMomentId": 86,
        "armId": 144,
        "variableId": 698,
        "measurementAttribute": "mean",
        "studyId": 44
      }, {
        "integerValue": 276,
        "realValue": null,
        "measurementMomentId": 86,
        "armId": 144,
        "variableId": 698,
        "measurementAttribute": "sample size",
        "studyId": 44
      }, {
        "integerValue": null,
        "realValue": 15.815811834996014,
        "measurementMomentId": 86,
        "armId": 144,
        "variableId": 698,
        "measurementAttribute": "standard deviation",
        "studyId": 44
      }]
    }, {
      "id": 145,
      "name": "Azilsartan Medoxomil 80 mg QD",
      "study": 44,
      "drugId": 98,
      "measurements": [{
        "integerValue": null,
        "realValue": -17.58,
        "measurementMomentId": 86,
        "armId": 145,
        "variableId": 698,
        "measurementAttribute": "mean",
        "studyId": 44
      }, {
        "integerValue": 279,
        "realValue": null,
        "measurementMomentId": 86,
        "armId": 145,
        "variableId": 698,
        "measurementAttribute": "sample size",
        "studyId": 44
      }, {
        "integerValue": null,
        "realValue": 15.818018554800092,
        "measurementMomentId": 86,
        "armId": 145,
        "variableId": 698,
        "measurementAttribute": "standard deviation",
        "studyId": 44
      }]
    }, {
      "id": 146,
      "name": "Olmesartan 40 mg QD",
      "study": 44,
      "drugId": 99,
      "measurements": [{
        "integerValue": null,
        "realValue": -14.87,
        "measurementMomentId": 86,
        "armId": 146,
        "variableId": 698,
        "measurementAttribute": "mean",
        "studyId": 44
      }, {
        "integerValue": 280,
        "realValue": null,
        "measurementMomentId": 86,
        "armId": 146,
        "variableId": 698,
        "measurementAttribute": "sample size",
        "studyId": 44
      }, {
        "integerValue": null,
        "realValue": 15.812874501494028,
        "measurementMomentId": 86,
        "armId": 146,
        "variableId": 698,
        "measurementAttribute": "standard deviation",
        "studyId": 44
      }]
    }, {
      "id": 147,
      "name": "Placebo QD",
      "study": 44,
      "drugId": 100,
      "measurements": [{
        "integerValue": null,
        "realValue": -2.06,
        "measurementMomentId": 86,
        "armId": 147,
        "variableId": 698,
        "measurementAttribute": "mean",
        "studyId": 44
      }, {
        "integerValue": 140,
        "realValue": null,
        "measurementMomentId": 86,
        "armId": 147,
        "variableId": 698,
        "measurementAttribute": "sample size",
        "studyId": 44
      }, {
        "integerValue": null,
        "realValue": 15.819597340008373,
        "measurementMomentId": 86,
        "armId": 147,
        "variableId": 698,
        "measurementAttribute": "standard deviation",
        "studyId": 44
      }]
    }, {
      "id": 143,
      "name": "Azilsartan Medoxomil 20 mg QD",
      "study": 44,
      "drugId": 98,
      "measurements": [{
        "integerValue": null,
        "realValue": -14.28,
        "measurementMomentId": 86,
        "armId": 143,
        "variableId": 698,
        "measurementAttribute": "mean",
        "studyId": 44
      }, {
        "integerValue": 274,
        "realValue": null,
        "measurementMomentId": 86,
        "armId": 143,
        "variableId": 698,
        "measurementAttribute": "sample size",
        "studyId": 44
      }, {
        "integerValue": null,
        "realValue": 15.824615761527987,
        "measurementMomentId": 86,
        "armId": 143,
        "variableId": 698,
        "measurementAttribute": "standard deviation",
        "studyId": 44
      }]
    }]
  }, {
    "studyId": 45,
    "name": "TAK491-011 / NCT00591253",
    "trialDataInterventions": [{
      "drugId": 102,
      "uri": "http://trials.drugis.org/namespaces/2/drug/6f8ce038bc50a5372fcaf86e4b300bb6",
      "studyId": 45
    }],
    "trialDataArms": [{
      "id": 149,
      "name": "Azilsartan Medoxomil 40 mg QD",
      "study": 45,
      "drugId": 101,
      "measurements": [{
        "integerValue": null,
        "realValue": -9.51,
        "measurementMomentId": 88,
        "armId": 149,
        "variableId": 731,
        "measurementAttribute": "mean",
        "studyId": 45
      }, {
        "integerValue": 134,
        "realValue": null,
        "measurementMomentId": 88,
        "armId": 149,
        "variableId": 731,
        "measurementAttribute": "sample size",
        "studyId": 45
      }, {
        "integerValue": null,
        "realValue": 15.395863080711,
        "measurementMomentId": 88,
        "armId": 149,
        "variableId": 731,
        "measurementAttribute": "standard deviation",
        "studyId": 45
      }]
    }, {
      "id": 150,
      "name": "Azilsartan Medoxomil 80 mg QD",
      "study": 45,
      "drugId": 101,
      "measurements": [{
        "integerValue": null,
        "realValue": -9.58,
        "measurementMomentId": 88,
        "armId": 150,
        "variableId": 731,
        "measurementAttribute": "mean",
        "studyId": 45
      }, {
        "integerValue": 130,
        "realValue": null,
        "measurementMomentId": 88,
        "armId": 150,
        "variableId": 731,
        "measurementAttribute": "sample size",
        "studyId": 45
      }, {
        "integerValue": null,
        "realValue": 15.403769993089353,
        "measurementMomentId": 88,
        "armId": 150,
        "variableId": 731,
        "measurementAttribute": "standard deviation",
        "studyId": 45
      }]
    }, {
      "id": 151,
      "name": "Placebo QD",
      "study": 45,
      "drugId": 102,
      "measurements": [{
        "integerValue": null,
        "realValue": -3.04,
        "measurementMomentId": 88,
        "armId": 151,
        "variableId": 731,
        "measurementAttribute": "mean",
        "studyId": 45
      }, {
        "integerValue": 133,
        "realValue": null,
        "measurementMomentId": 88,
        "armId": 151,
        "variableId": 731,
        "measurementAttribute": "sample size",
        "studyId": 45
      }, {
        "integerValue": null,
        "realValue": 15.37290593869617,
        "measurementMomentId": 88,
        "armId": 151,
        "variableId": 731,
        "measurementAttribute": "standard deviation",
        "studyId": 45
      }]
    }]
  }, {
    "studyId": 46,
    "name": "TAK491-019 / NCT00696436",
    "trialDataInterventions": [{
      "drugId": 106,
      "uri": "http://trials.drugis.org/namespaces/2/drug/6f8ce038bc50a5372fcaf86e4b300bb6",
      "studyId": 46
    }, {
      "drugId": 105,
      "uri": "http://trials.drugis.org/namespaces/2/drug/d7d7c477b89a5e4e5a57318743ccc87e",
      "studyId": 46
    }],
    "trialDataArms": [{
      "id": 156,
      "name": "Olmesartan 40 mg QD",
      "study": 46,
      "drugId": 105,
      "measurements": [{
        "integerValue": null,
        "realValue": -13.2,
        "measurementMomentId": 90,
        "armId": 156,
        "variableId": 758,
        "measurementAttribute": "mean",
        "studyId": 46
      }, {
        "integerValue": 283,
        "realValue": null,
        "measurementMomentId": 90,
        "armId": 156,
        "variableId": 758,
        "measurementAttribute": "sample size",
        "studyId": 46
      }, {
        "integerValue": null,
        "realValue": 15.729134591578775,
        "measurementMomentId": 90,
        "armId": 156,
        "variableId": 758,
        "measurementAttribute": "standard deviation",
        "studyId": 46
      }]
    }, {
      "id": 155,
      "name": "Valsartan 320 mg QD",
      "study": 46,
      "drugId": 104,
      "measurements": [{
        "integerValue": null,
        "realValue": -11.31,
        "measurementMomentId": 90,
        "armId": 155,
        "variableId": 758,
        "measurementAttribute": "mean",
        "studyId": 46
      }, {
        "integerValue": 271,
        "realValue": null,
        "measurementMomentId": 90,
        "armId": 155,
        "variableId": 758,
        "measurementAttribute": "sample size",
        "studyId": 46
      }, {
        "integerValue": null,
        "realValue": 15.721284139662384,
        "measurementMomentId": 90,
        "armId": 155,
        "variableId": 758,
        "measurementAttribute": "standard deviation",
        "studyId": 46
      }]
    }, {
      "id": 157,
      "name": "Placebo QD",
      "study": 46,
      "drugId": 106,
      "measurements": [{
        "integerValue": null,
        "realValue": -1.83,
        "measurementMomentId": 90,
        "armId": 157,
        "variableId": 758,
        "measurementAttribute": "mean",
        "studyId": 46
      }, {
        "integerValue": 148,
        "realValue": null,
        "measurementMomentId": 90,
        "armId": 157,
        "variableId": 758,
        "measurementAttribute": "sample size",
        "studyId": 46
      }, {
        "integerValue": null,
        "realValue": 15.730023903351194,
        "measurementMomentId": 90,
        "armId": 157,
        "variableId": 758,
        "measurementAttribute": "standard deviation",
        "studyId": 46
      }]
    }, {
      "id": 154,
      "name": "Azilsartan Medoxomil 80 mg QD",
      "study": 46,
      "drugId": 103,
      "measurements": [{
        "integerValue": null,
        "realValue": -16.74,
        "measurementMomentId": 90,
        "armId": 154,
        "variableId": 758,
        "measurementAttribute": "mean",
        "studyId": 46
      }, {
        "integerValue": 270,
        "realValue": null,
        "measurementMomentId": 90,
        "armId": 154,
        "variableId": 758,
        "measurementAttribute": "sample size",
        "studyId": 46
      }, {
        "integerValue": null,
        "realValue": 15.725114625973317,
        "measurementMomentId": 90,
        "armId": 154,
        "variableId": 758,
        "measurementAttribute": "standard deviation",
        "studyId": 46
      }]
    }, {
      "id": 153,
      "name": "Azilsartan Medoxomil 40 mg QD",
      "study": 46,
      "drugId": 103,
      "measurements": [{
        "integerValue": null,
        "realValue": -16.38,
        "measurementMomentId": 90,
        "armId": 153,
        "variableId": 758,
        "measurementAttribute": "mean",
        "studyId": 46
      }, {
        "integerValue": 269,
        "realValue": null,
        "measurementMomentId": 90,
        "armId": 153,
        "variableId": 758,
        "measurementAttribute": "sample size",
        "studyId": 46
      }, {
        "integerValue": null,
        "realValue": 15.728769468715601,
        "measurementMomentId": 90,
        "armId": 153,
        "variableId": 758,
        "measurementAttribute": "standard deviation",
        "studyId": 46
      }]
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
      "numberOfStudies": 2
    }]
  };

  function exampleInterventions() {
    return [{
      name: 'intervention 1',
      semanticInterventionUri: 'http://trials.drugis.org/namespaces/1/drug/a4b119795fa42c624640a77ce024d9a2'
    }, {
      name: 'intervention 2',
      semanticInterventionUri: 'http://trials.drugis.org/namespaces/1/drug/a0f638328eeea353bf0ba7f111a167dd'
    }, {
      name: 'intervention 3',
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
            studyRowSpan: 3,
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
            trialverseId: 86
          });

          expect(resultRows[1]).toEqual({
            arm: 'Sertraline',
            intervention: 'intervention 1',
            mu: null,
            rate: 1,
            sampleSize: 96,
            sigma: null,
            study: 'Fava et al, 2002',
            studyRowSpan: 3,
            included: true,
            trialverseId: 85
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
            studyRowSpan: 3,
            included: true,
            trialverseId: 87
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

  });
});