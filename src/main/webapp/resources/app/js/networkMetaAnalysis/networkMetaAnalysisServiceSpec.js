define([
  'angular-mocks',
  './networkMetaAnalysis',
  '../util/resultsConstants',
], function() {
  var CONTINUOUS_TYPE = 'http://trials.drugis.org/ontology#continuous';

  var exampleStudies = [{
    studyUri: 'http://trials.drugis.org/graphs/favaUuid',
    name: 'Fava et al, 2002',
    defaultMeasurementMoment: 'defaultMMUri',
    measurementMoments: [{
      uri: 'defaultMMUri'
    }],
    arms: [{
      uri: 'http://trials.drugis.org/instances/fava-parox-arm',
      name: 'Paroxetine',
      drugInstance: 'http://trials.drugis.org/instances/parox-instance',
      measurements: {
        defaultMMUri: [{
          measurementTypeURI: CONTINUOUS_TYPE,
          studyUuid: 'http://trials.drugis.org/graphs/favaUuid',
          variableUri: 'http://trials.drugis.org/instances/hamd-instance',
          variableConceptUri: 'variableConceptUri',
          armUri: 'http://trials.drugis.org/instances/fava-parox-arm',
          sampleSize: 96,
          rate: null,
          stdDev: null,
          mean: 3.5,
          exposure: null
        }]
      },
      semanticIntervention: {
        drugInstance: 'http://trials.drugis.org/instances/parox-instance',
        drugConcept: 'http://trials.drugis.org/concepts/parox-concept'
      },
      matchedProjectInterventionIds: [2]
    }, {
      uri: 'http://trials.drugis.org/instances/fava-sertra-arm',
      name: 'Sertraline',
      drugInstance: 'http://trials.drugis.org/instances/sertra-instance',
      measurements: {
        defaultMMUri: [{
          measurementTypeURI: CONTINUOUS_TYPE,
          studyUuid: 'http://trials.drugis.org/graphs/favaUuid',
          variableUri: 'http://trials.drugis.org/instances/hamd-instance',
          variableConceptUri: 'variableConceptUri',
          armUri: 'http://trials.drugis.org/instances/fava-sertra-arm',
          sampleSize: 96,
          rate: null,
          stdDev: null,
          mean: 4.5,
          exposure: null
        }]
      },
      semanticIntervention: {
        drugInstance: 'http://trials.drugis.org/instances/sertra-instance',
        drugConcept: 'http://trials.drugis.org/concepts/sertra-concept'
      },
      matchedProjectInterventionIds: [3]
    }, {
      uri: 'http://trials.drugis.org/instances/fava-arm-1-uri',
      name: 'Fluoxetine',
      drugInstance: 'http://trials.drugis.org/instances/fluoxInstance',
      measurements: {
        defaultMMUri: [{
          measurementTypeURI: CONTINUOUS_TYPE,
          studyUuid: 'http://trials.drugis.org/graphs/favaUuid',
          variableUri: 'http://trials.drugis.org/instances/hamd-instance',
          variableConceptUri: 'variableConceptUri',
          armUri: 'http://trials.drugis.org/instances/fava-arm-1-uri',
          sampleSize: 92,
          rate: null,
          stdDev: null,
          mean: 5.5,
          exposure: null
        }]
      },
      semanticIntervention: {
        drugInstance: 'http://trials.drugis.org/instances/fluoxInstance',
        drugConcept: 'http://trials.drugis.org/concepts/fluox-concept'
      },
      matchedProjectInterventionIds: [1]
    }],
    covariateValues: [{
      covariateKey: 'COVARIATE_KEY',
      value: 123
    }]
  }];

  var exampleNetworkStudies = [{
    studyUri: '44',
    name: 'TAK491-008 / NCT00696241',
    defaultMeasurementMoment: 'defaultMMUri',
    covariateValues: [{
      covariateKey: 'COVARIATE_KEY',
      value: 123
    },],
    arms: [{
      uri: '144',
      name: 'Azilsartan Medoxomil 40 mg QD',
      drugInstance: '98',
      measurements: {
        defaultMMUri: [{
          measurementTypeURI: CONTINUOUS_TYPE,
          mean: -14.47,
          armUri: '144',
          variableUri: 698,
          variableConceptUri: 'variableConceptUri1',
          'measurementAttribute': 'mean',
          sampleSize: 276,
          stdDev: 15.815811834996014
        }]
      },
      semanticIntervention: {
        drugInstance: '98',
        drugConcept: 'http://trials.drugis.org/concepts/azi-concept'
      },
      matchedProjectInterventionIds: [null]
    }, {
      uri: '145',
      name: 'Azilsartan Medoxomil 80 mg QD',
      'study': '44',
      drugInstance: '98',
      measurements: {
        defaultMMUri: [{
          measurementTypeURI: CONTINUOUS_TYPE,
          mean: -17.58,
          armUri: '145',
          variableUri: '698',
          variableConceptUri: 'variableConceptUri1',
          sampleSize: 279,
          stdDev: 15.818018554800092
        }]
      },
      semanticIntervention: {
        drugInstance: '98',
        drugConcept: 'http://trials.drugis.org/concepts/azi-concept'
      },
      matchedProjectInterventionIds: [null]
    }, {
      uri: '146',
      name: 'Olmesartan 40 mg QD',
      drugInstance: '99',
      measurements: {
        defaultMMUri: [{
          measurementTypeURI: CONTINUOUS_TYPE,
          mean: -14.87,
          armUri: '146',
          variableUri: '698',
          variableConceptUri: 'variableConceptUri1',
          sampleSize: 280,
          stdDev: 15.812874501494028
        }]
      },
      semanticIntervention: {
        drugInstance: '98',
        drugConcept: 'http://trials.drugis.org/concepts/olme-concept'
      },

      matchedProjectInterventionIds: [44]

    }, {
      uri: '147',
      name: 'Placebo QD',
      drugInstance: '100',
      measurements: {
        defaultMMUri: [{
          measurementTypeURI: CONTINUOUS_TYPE,
          mean: -2.06,
          armUri: '147',
          'variabletUri': '698',
          variableConceptUri: 'variableConceptUri1',
          sampleSize: 140,
          stdDev: 15.819597340008373
        }]
      },
      semanticIntervention: {
        drugInstance: '98',
        drugConcept: 'http://trials.drugis.org/concepts/placebo-concept'
      },
      matchedProjectInterventionIds: [43]
    }, {
      uri: 143,
      name: 'Azilsartan Medoxomil 20 mg QD',
      drugInstance: '98',
      measurements: {
        defaultMMUri: [{
          measurementTypeURI: CONTINUOUS_TYPE,
          mean: -14.28,
          armUri: '143',
          variableUri: 698,
          variableConceptUri: 'variableConceptUri1',
          sampleSize: 274,
          stdDev: 15.824615761527987
        }]
      },
      semanticIntervention: {
        drugInstance: '98',
        drugConcept: 'http://trials.drugis.org/concepts/azi-concept'
      },
      matchedProjectInterventionIds: [null]
    }]
  }, {
    studyUri: '45',
    name: 'TAK491-011 / NCT00591253',
    defaultMeasurementMoment: 'defaultMMUri',
    arms: [{
      uri: '149',
      name: 'Azilsartan Medoxomil 40 mg QD',
      drugInstance: '101',
      measurements: {
        defaultMMUri: [{
          measurementTypeURI: CONTINUOUS_TYPE,
          mean: -9.51,
          armUri: '149',
          variableUri: '731',
          variableConceptUri: 'variableConceptUri1',
          'measurementAttribute': 'mean',
          sampleSize: 134,
          stdDev: 15.395863080711
        }]
      },
      semanticIntervention: {
        drugInstance: '98',
        drugConcept: 'http://trials.drugis.org/concepts/azi-concept'
      },
      matchedProjectInterventionIds: [null]
    }, {
      uri: '150',
      name: 'Azilsartan Medoxomil 80 mg QD',
      drugInstance: '101',
      measurements: {
        defaultMMUri: [{
          measurementTypeURI: CONTINUOUS_TYPE,
          mean: -9.58,
          armUri: '150',
          variableUri: '731',
          variableConceptUri: 'variableConceptUri1',
          'measurementAttribute': 'mean',
          sampleSize: 130,
          stdDev: 15.403769993089353
        }]
      },
      semanticIntervention: {
        drugInstance: '98',
        drugConcept: 'http://trials.drugis.org/concepts/azi-concept'
      },
      matchedProjectInterventionIds: [null]
    }, {
      uri: '151',
      name: 'Placebo QD',
      drugInstance: '102',
      measurements: {
        defaultMMUri: [{
          measurementTypeURI: CONTINUOUS_TYPE,
          mean: -3.04,
          armUri: 151,
          variableUri: 731,
          variableConceptUri: 'variableConceptUri1',
          sampleSize: 271,
          stdDev: 15.37290593869617
        }]
      },
      semanticIntervention: {
        drugInstance: '98',
        drugConcept: 'http://trials.drugis.org/concepts/placebo-concept'
      },
      matchedProjectInterventionIds: [43]
    }]
  }, {
    studyUri: '46',
    name: 'TAK491-019 / NCT00696436',
    defaultMeasurementMoment: 'defaultMMUri',
    arms: [{
      uri: '156',
      name: 'Olmesartan 40 mg QD',
      drugInstance: '105',
      measurements: {
        defaultMMUri: [{
          measurementTypeURI: CONTINUOUS_TYPE,
          mean: -13.2,
          armUri: '156',
          variableUri: '758',
          variableConceptUri: 'variableConceptUri1',
          sampleSize: 283,
          stdDev: 15.729134591578775
        }]
      },
      semanticIntervention: {
        drugInstance: '98',
        drugConcept: 'http://trials.drugis.org/concepts/olme-concept'
      },
      matchedProjectInterventionIds: [44]
    }, {
      uri: 155,
      name: 'Valsartan 320 mg QD',
      drugInstance: '104',
      measurements: {
        defaultMMUri: [{
          measurementTypeURI: CONTINUOUS_TYPE,
          mean: -11.31,
          armUri: '155',
          variableUri: '758',
          variableConceptUri: 'variableConceptUri1',
          sampleSize: 271,
          stdDev: 15.721284139662384
        }]
      },
      semanticIntervention: {
        drugInstance: '98',
        drugConcept: 'http://trials.drugis.org/concepts/valsa-concept'
      },
      matchedProjectInterventionIds: [null]
    }, {
      uri: 157,
      name: 'Placebo QD',
      drugInstance: '106',
      measurements: {
        defaultMMUri: [{
          measurementTypeURI: CONTINUOUS_TYPE,
          mean: -1.83,
          armUri: '157',
          variableUri: '758',
          variableConceptUri: 'variableConceptUri1',
          sampleSize: 148,
          stdDev: 15.730023903351194
        }]
      },
      semanticIntervention: {
        drugInstance: '98',
        drugConcept: 'http://trials.drugis.org/concepts/placebo-concept'
      },
      matchedProjectInterventionIds: [43]
    }, {
      uri: 154,
      name: 'Azilsartan Medoxomil 80 mg QD',
      drugInstance: '103',
      measurements: {
        defaultMMUri: [{
          measurementTypeURI: CONTINUOUS_TYPE,
          realValue: -16.74,
          armUri: 154,
          variableUri: 758,
          variableConceptUri: 'variableConceptUri1',
          stdDev: 15.725114625973317
        }]
      },
      semanticIntervention: {
        drugInstance: '98',
        drugConcept: 'http://trials.drugis.org/concepts/azi-concept'
      },
      matchedProjectInterventionIds: [null]
    }, {
      uri: '153',
      name: 'Azilsartan Medoxomil 40 mg QD',
      drugInstance: '103',
      measurements: {
        defaultMMUri: [{
          measurementTypeURI: CONTINUOUS_TYPE,
          mean: -16.38,
          armUri: '153',
          variableUri: '758',
          variableConceptUri: 'variableConceptUri1',
          sampleSize: 269,
          stdDev: 15.728769468715601
        }]
      },
      semanticIntervention: {
        drugInstance: '98',
        drugConcept: 'http://trials.drugis.org/concepts/azi-concept'
      },
      matchedProjectInterventionIds: [null]
    }]
  }];

  var expectedNetwork = {
    interventions: [{
      name: 'placebo',
      sampleSize: 559
    }, {
      name: 'Olmes',
      sampleSize: 563
    }, {
      name: 'Chlora',
      sampleSize: 0
    }],
    edges: [{
      from: {
        id: 43,
        project: 13,
        name: 'placebo',
        motivation: '',
        semanticInterventionLabel: 'Placebo',
        semanticInterventionUri: 'http://trials.drugis.org/concepts/placebo-concept'
      },
      to: {
        id: 44,
        project: 13,
        name: 'Olmes',
        motivation: '',
        semanticInterventionLabel: 'Olmesartan',
        semanticInterventionUri: 'http://trials.drugis.org/concepts/olme-concept'
      },
      studies: [exampleNetworkStudies[0], exampleNetworkStudies[2]]
    }]
  };

  function exampleInterventions() {
    return [{
      id: 1,
      name: 'intervention 1',
      semanticInterventionUri: 'http://trials.drugis.org/namespaces/1/drug/a4b119795fa42c624640a77ce024d9a2'
    }, {
      id: 2,
      name: 'intervention 2',
      semanticInterventionUri: 'http://trials.drugis.org/namespaces/1/drug/a0f638328eeea353bf0ba7f111a167dd'
    }, {
      id: 3,
      name: 'intervention 3',
      semanticInterventionUri: 'http://trials.drugis.org/concepts/87fec8a8071915a2e17eddeb1faf8daa'
    }];
  }

  var networkInterventions = [{
    'id': 43,
    'project': 13,
    'name': 'placebo',
    'motivation': '',
    'semanticInterventionLabel': 'Placebo',
    'semanticInterventionUri': 'http://trials.drugis.org/concepts/placebo-concept'
  }, {
    'id': 44,
    'project': 13,
    'name': 'Olmes',
    'motivation': '',
    'semanticInterventionLabel': 'Olmesartan',
    'semanticInterventionUri': 'http://trials.drugis.org/concepts/olme-concept'
  }, {
    'id': 45,
    'project': 13,
    'name': 'Chlora',
    'motivation': '',
    'semanticInterventionLabel': 'Chlortalidone',
    'semanticInterventionUri': 'http://trials.drugis.org/concepts/a977e3a6fa4dc0a34fcf9fb351bc0a0e'
  }];

  var networkMetaAnalysisService;
  describe('The networkMetaAnalysisService', function() {
    var analysisServiceMock = jasmine.createSpyObj('AnalysisService', ['generateEdges']);

    beforeEach(angular.mock.module('addis.networkMetaAnalysis', function($provide) {
      $provide.value('AnalysisService', analysisServiceMock);
    }));

    beforeEach(inject(function(NetworkMetaAnalysisService) {
      networkMetaAnalysisService = NetworkMetaAnalysisService;
    }));

    describe('transformStudiesToNetwork', function() {
      beforeEach(function() {
        analysisServiceMock.generateEdges.and.returnValue(expectedNetwork.edges);
      });

      it('should construct the evidence network from the list of trialDataStudies', function() {
        var studies = exampleNetworkStudies;
        var analysis = {
          excludedArms: [],
          outcome: {
            semanticOutcomeUri: 'variableConceptUri1'
          }
        };
        var momentSelections = {};
        momentSelections[studies[0].studyUri] = {
          uri: 'defaultMMUri'
        };
        momentSelections[studies[1].studyUri] = {
          uri: 'defaultMMUri'
        };
        momentSelections[studies[2].studyUri] = {
          uri: 'defaultMMUri'
        };
        var network = networkMetaAnalysisService.transformStudiesToNetwork(studies, networkInterventions, analysis, momentSelections);
        expect(network).toEqual(expectedNetwork);
      });

    });

    describe('transformStudiesToTableRows', function() {
      it('should construct table rows from the list of trialDataStudies', function() {
        var treatmentOverlapMap = {};
        var interventions = exampleInterventions();
        var excludedArms = [];
        var analysis = {
          excludedArms: excludedArms,
          outcome: {
            semanticOutcomeUri: 'variableConceptUri'
          }
        };

        var covariates = [{
          isIncluded: false
        }, {
          isIncluded: true,
          name: 'covariate name',
          definitionKey: 'COVARIATE_KEY'
        }, {
          isIncluded: false
        }];

        var result = networkMetaAnalysisService.transformStudiesToTableRows(exampleStudies, interventions, analysis, covariates, treatmentOverlapMap);
        var study1 = {
          covariatesColumns: [{
            headerTitle: 'covariate name',
            data: 123
          }],
          study: 'Fava et al, 2002',
          studyUri: 'http://trials.drugis.org/graphs/favaUuid',
          studyUuid: 'favaUuid',
          studyRowSpan: 3,
          arm: 'Fluoxetine',
          trialverseUid: 'http://trials.drugis.org/instances/fava-arm-1-uri',
          included: true,
          intervention: 'intervention 1',
          interventionId: 1,
          interventionRowSpan: 1,
          measurements: {
            defaultMMUri: {
              type: 'continuous',
              rate: 'NA',
              mu: 5.5,
              sigma: 'NA',
              sampleSize: 92,
              stdErr: 'NA',
              exposure: 'NA'
            }
          },
          firstInterventionRow: true,
          firstStudyRow: true,
          measurementMoments: exampleStudies[0].measurementMoments,
          numberOfMatchedInterventions: 3,
          numberOfIncludedInterventions: 3,
          isContrastArm: false
        };
        var study2 = {
          covariatesColumns: [{
            headerTitle: 'covariate name',
            data: 123
          }],
          study: 'Fava et al, 2002',
          studyUri: 'http://trials.drugis.org/graphs/favaUuid',
          studyUuid: 'favaUuid',
          studyRowSpan: 3,
          arm: 'Paroxetine',
          trialverseUid: 'http://trials.drugis.org/instances/fava-parox-arm',
          included: true,
          intervention: 'intervention 2',
          interventionId: 2,
          measurements: {
            defaultMMUri: {
              type: 'continuous',
              rate: 'NA',
              mu: 3.5,
              sigma: 'NA',
              sampleSize: 96,
              stdErr: 'NA',
              exposure: 'NA'
            }
          },
          measurementMoments: exampleStudies[0].measurementMoments,
          numberOfMatchedInterventions: 3,
          numberOfIncludedInterventions: 3,
          firstInterventionRow: true,
          interventionRowSpan: 1,
          isContrastArm: false
        };
        var study3 = {
          covariatesColumns: [{
            headerTitle: 'covariate name',
            data: 123
          }],
          study: 'Fava et al, 2002',
          studyUri: 'http://trials.drugis.org/graphs/favaUuid',
          studyUuid: 'favaUuid',
          studyRowSpan: 3,
          arm: 'Sertraline',
          trialverseUid: 'http://trials.drugis.org/instances/fava-sertra-arm',
          included: true,
          intervention: 'intervention 3',
          interventionId: 3,
          measurements: {
            defaultMMUri: {
              type: 'continuous',
              rate: 'NA',
              mu: 4.5,
              sigma: 'NA',
              sampleSize: 96,
              stdErr: 'NA',
              exposure: 'NA'
            }
          },
          measurementMoments: exampleStudies[0].measurementMoments,
          numberOfMatchedInterventions: 3,
          numberOfIncludedInterventions: 3,
          firstInterventionRow: true,
          interventionRowSpan: 1,
          isContrastArm: false
        };

        expect(result.absolute[0]).toEqual(study1);
        expect(result.absolute[1]).toEqual(study2);
        expect(result.absolute[2]).toEqual(study3);
      });
    });

    describe('changeArmExclusion', function() {
      it('should add an excluded arm if dataRow.included is false', function() {
        var dataRow = {
          included: false,
          trialverseUid: -1
        };

        var analysis = {
          id: 1,
          excludedArms: [{
            trialverseUid: -2,
            analysisId: 1
          }],
        };

        analysis = networkMetaAnalysisService.changeArmExclusion(dataRow, analysis);
        expect(analysis.excludedArms.length).toBe(2);
      });

      it('should remove an excluded arm if datarow.included = true for the corresponding arm', function() {
        var row = {
          included: true,
          trialverseUid: -2
        };
        var analysis = {
          id: 1,
          excludedArms: [{
            trialverseUid: -2,
            analysisId: 1
          }, {
            trialverseUid: -3,
            analysisId: 1
          }],
        };
        var result = networkMetaAnalysisService.changeArmExclusion(row, analysis);
        expect(result.excludedArms.length).toBe(1);
        expect(result.excludedArms[0].trialverseUid).toBe(-3);
      });
    });

    describe('doesInterventionHaveAmbiguousArms', function() {
      it('should return true if there are ambiguous arms for the intervention', function() {
        var interventionId = 1;
        var studyUri = '27';
        var studies = [{
          studyUri: studyUri,
          arms: [{
            matchedProjectInterventionIds: [1],
            interventionId: '1'
          }, {
            matchedProjectInterventionIds: [1],
            interventionId: '1'
          }]
        }];
        var analysis = {
          excludedArms: []
        };
        var result = networkMetaAnalysisService.doesInterventionHaveAmbiguousArms(interventionId, studyUri, studies, analysis);
        expect(result).toBeTruthy();
      });

      it('should return false if there are no ambiguous arms for the intervention', function() {
        var interventionId = 1;
        var studyUri = '27';
        var trialverseData = [{
          studyUri: studyUri,
          arms: [{
            interventionId: 1
          }, {
            interventionId: 2
          }]
        }];
        var analysis = {
          excludedArms: []
        };
        expect(networkMetaAnalysisService.doesInterventionHaveAmbiguousArms(interventionId, studyUri, trialverseData, analysis)).toBeFalsy();
      });

      it('should return false if the ambiguity has been resolved through exclusion', function() {
        var interventionId = 1;
        var studyUri = '27';
        var trialverseData = [{
          studyUri: studyUri,
          arms: [{
            id: 3,
            interventionId: 1
          }, {
            id: 4,
            interventionId: 1
          }]
        }];
        var analysis = {
          excludedArms: [{
            trialverseUid: 3
          }]
        };
        expect(networkMetaAnalysisService.doesInterventionHaveAmbiguousArms(interventionId, studyUri, trialverseData, analysis)).toBeFalsy();
      });
    });

    describe('doesModelHaveAmbiguousArms', function() {
      it('should return true if there are ambiguous arms for the model', function() {
        var interventions = [{
          interventionId: 1,
          semanticInterventionUri: 'uri1'
        }, {
          interventionId: 2,
          semanticInterventionUri: 'uri2'
        }, {
          interventionId: 3,
          semanticInterventionUri: 'uri3'
        }];
        var studies = [{
          studyUri: 'studyUri',
          arms: [{
            drugInstance: 1,
            interventionId: 'uri1',
            matchedProjectInterventionIds: [1, 2, 3]
          }, {
            drugInstance: 2,
            interventionId: 'uri1',
            matchedProjectInterventionIds: [1, 2, 3]
          }, {
            drugInstance: 3,
            interventionId: 'uri1',
            matchedProjectInterventionIds: [1, 2, 3]
          }]
        }];
        var analysis = {
          excludedArms: [],
          interventionInclusions: interventions
        };
        var result = networkMetaAnalysisService.doesModelHaveAmbiguousArms(studies, analysis);
        expect(result).toBeTruthy();
      });
    });

    describe('addInclusionsToInterventions', function() {
      it('should add inclusions to interventions', function() {
        var inclusions = [{
          interventionId: 43
        }];
        var includedInterventions = networkMetaAnalysisService.addInclusionsToInterventions(networkInterventions, inclusions);
        expect(includedInterventions[0].isIncluded).toBeTruthy();
        expect(includedInterventions[1].isIncluded).toBeFalsy();
        expect(includedInterventions[2].isIncluded).toBeFalsy();
      });
    });

    describe('buildInterventionInclusions', function() {
      it('should create a new list of intervention inclusions', function() {
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
        var interventionExclusions = networkMetaAnalysisService.buildInterventionInclusions(interventions, analysis);
        expect(interventionExclusions).toEqual([{
          analysisId: 4,
          interventionId: 1
        }, {
          analysisId: 4,
          interventionId: 3
        }]);
      });
    });

    describe('buildMomentSelections', function() {
      it('should create a map where for each study the default moment is selected except in cases where there is a selection on the analysis', function() {
        var study1 = {
          studyUri: 'studyUri1',
          defaultMeasurementMoment: 'defaultMoment1',
          measurementMoments: [{
            uri: 'defaultMoment1'
          }, {
            uri: 'customMoment1'
          }]
        };
        var study2 = {
          studyUri: 'studyUri2',
          defaultMeasurementMoment: 'defaultMoment2',
          measurementMoments: [{
            uri: 'defaultMoment2'
          }, {
            uri: 'customMoment2'
          }]
        };
        var study3 = {
          studyUri: 'studyUri3',
          defaultMeasurementMoment: 'momentNotInData',
          measurementMoments: [{
            uri: 'customMoment3'
          }]
        };
        var trialData = [study1, study2, study3];
        var analysis = {
          includedMeasurementMoments: [{
            study: 'studyUri2',
            measurementMoment: 'customMoment2'
          }]
        };
        var expectedResult = {
          studyUri1: study1.measurementMoments[0],
          studyUri2: study2.measurementMoments[1],
          studyUri3: study3.measurementMoments[0]
        };
        var result = networkMetaAnalysisService.buildMomentSelections(trialData, analysis);
        expect(result).toEqual(expectedResult);
      });
    });

    describe('cleanUpExcludedArms', function() {
      it('should remove armExclusions that match the intervention', function() {
        var study1 = {
          arms: [{
            drugInstance: 1,
            uri: 10,
            semanticIntervention: {
              drugConcept: 'uri1'
            },
            matchedProjectInterventionIds: [-3]
          }, {
            drugInstance: 2,
            uri: 11,
            semanticIntervention: {
              drugConcept: 'uri2'
            },
            matchedProjectInterventionIds: [-3]
          }, {
            drugInstance: 2,
            uri: 12,
            semanticIntervention: {
              drugConcept: 'uri2'
            },
            matchedProjectInterventionIds: [-4]
          }]
        };

        var trialDataStudies = [study1];

        var analysis = {
          excludedArms: [{
            trialverseUid: 10
          }, {
            trialverseUid: 21
          }]
        };
        var intervention = {
          id: -3,
          semanticInterventionUri: 'uri1'
        };

        var expectedArmExclusions = [{
          trialverseUid: 21
        }];

        expect(networkMetaAnalysisService.cleanUpExcludedArms(intervention, analysis, trialDataStudies)).toEqual(expectedArmExclusions);

      });
    });

    describe('buildOverlappingTreatmentMap', function() {
      it('should create an empty object if there is no overlap', function() {
        var interventions = [{
          id: 1,
          semanticInterventionUri: 'drugUri1',
          isIncluded: true
        }];
        var trialData = [{
          studyUuid: 'studyUuid',
          arms: [{
            interventionId: 'drugUri1',
            matchedProjectInterventionIds: [1]
          }, {
            interventionId: 'druguri2',
            matchedProjectInterventionIds: [2]
          }]
        }];

        var overlapMap = networkMetaAnalysisService.buildOverlappingTreatmentMap(interventions, trialData);
        expect(overlapMap).toEqual({});
      });

      it('should create a list of overlapping interventions for each intervention if they overlap', function() {
        var intervention1 = {
          id: 1,
          isIncluded: true
        };
        var intervention2 = {
          id: 2,
          isIncluded: true
        };
        var intervention3 = {
          id: 3,
          isIncluded: true
        };

        var interventions = [intervention1, intervention2, intervention3];
        var trialData = [{
          studyUuid: 'studyUuid',
          arms: [{
            matchedProjectInterventionIds: [1, 2]
          }, {
            matchedProjectInterventionIds: [2, 3]
          }]
        }];
        var expectedMap = {
          1: [intervention2],
          2: [intervention1, intervention3],
          3: [intervention2]
        };

        var overlapMap = networkMetaAnalysisService.buildOverlappingTreatmentMap(interventions, trialData);
        expect(overlapMap).toEqual(expectedMap);
      });
    });

    describe('buildMissingValueByStudy', function() {
      it('should build a map indexed by study uri where studies with missing values are truthy', function() {
        var row1 = {
          referenceArm: 'referenceArmUri',
          trialverseUid: 'referenceArmUri',
          studyUri: 'study1Uri',
          included: true,
          isContrastArm: true
        };
        var row2 = {
          referenceArm: 'referenceArmUri',
          trialverseUid: 'nonRefArmUri',
          studyUri: 'study1Uri',
          measurements: {
            defaultMMUri: {
              referenceArm: 'referenceArmUri',
              stdErr: 1,
              meanDifference: 2,
              measurementTypeURI: 'continuous'
            }
          },
          included: true,
          isContrastArm: true
        };
        var row3 = {
          referenceArm: 'referenceArmUri',
          trialverseUid: 'nonRefArmUri',
          studyUri: 'study2Uri',
          measurements: {
            defaultMMUri: {
              referenceArm: 'referenceArmUri',
              stdErr: 1,
              measurementTypeURI: 'continuous'
            }
          },
          included: true,
          isContrastArm: true
        };
        var row4 = {
          trialverseUid: 'nonRefArmUri',
          studyUri: 'study3Uri',
          measurements: {
            defaultMMUri: {
              stdErr: 1,
              measurementTypeURI: 'continuous'
            }
          },
          included: true
        };
        var momentSelections = {
          study1Uri: {
            uri: 'defaultMMUri'
          },
          study2Uri: {
            uri: 'defaultMMUri'
          },
          study3Uri: {
            uri: 'defaultMMUri'
          }
        };
        var rows = [row1, row2, row3, row4];
        var result = networkMetaAnalysisService.buildMissingValueByStudy(rows, momentSelections);

        var expectedResult = {
          study1Uri: false,
          study2Uri: true,
          study3Uri: true
        };
        expect(result).toEqual(expectedResult);
      });
    });

    describe('doesModelHaveInsufficientCovariateValues', function() {
      it('should find covariates with only one level', function() {
        var goodTrialData = [{
          covariatesColumns: [{
            headerTitle: 'cov 1',
            data: 1
          }]
        }, {
          covariatesColumns: [{
            headerTitle: 'cov 1',
            data: 2
          }]
        }];
        var badtrialData = angular.copy(goodTrialData);
        badtrialData[1].covariatesColumns[0].data = 1;

        expect(networkMetaAnalysisService.doesModelHaveInsufficientCovariateValues(goodTrialData)).toBeFalsy();
        expect(networkMetaAnalysisService.doesModelHaveInsufficientCovariateValues(badtrialData)).toBeTruthy();
      });
    });

    describe('getAbsoluteColumnsToShow', function() {
      describe('for continuous outcomes', function() {
        it('should be correct for some missing values', function() {
          var rows = [{
            measurements: [{
              sigma: 0.3,
              sampleSize: 0.4,
              stdErr: 'NA',
              type: 'continuous'
            }, {
              sigma: 0.3,
              sampleSize: 'NA',
              stdErr: 4,
              type: 'continuous'
            }]
          }];
          var expectedResult = {
            rate: false,
            mu: true,
            sigma: true,
            sampleSize: true,
            stdErr: true,
            exposure: false
          };
          expect(networkMetaAnalysisService.getAbsoluteColumnsToShow(rows)).toEqual(expectedResult);
        });
        it('should not show sigma if it has no values', function() {
          var rows = [{
            measurements: [{
              sigma: 'NA',
              sampleSize: 'NA',
              stdErr: 1.3,
              type: 'continuous'
            }, {
              sigma: 'NA',
              sampleSize: 31,
              stdErr: 4,
              type: 'continuous'
            }]
          }];
          var expectedResult = {
            rate: false,
            mu: true,
            sigma: false,
            sampleSize: true,
            stdErr: true,
            exposure: false
          };
          expect(networkMetaAnalysisService.getAbsoluteColumnsToShow(rows)).toEqual(expectedResult);
        });
        it('should not show N if there is no data', function() {
          var rows = [{
            measurements: [{
              sigma: 'NA',
              sampleSize: 'NA',
              stdErr: 1.3,
              type: 'continuous'
            }, {
              sigma: 'NA',
              sampleSize: 'NA',
              stdErr: 4,
              type: 'continuous'
            }]
          }];
          var expectedResult = {
            rate: false,
            mu: true,
            sigma: false,
            sampleSize: false,
            stdErr: true,
            exposure: false
          };
          expect(networkMetaAnalysisService.getAbsoluteColumnsToShow(rows)).toEqual(expectedResult);
        });
      });
    });

    describe('hasInterventionOverlap', () => {
      it('should return true if there are interventions in the overlap map', () => {
        var interventionOverlapMap = {
          intervention1: {},
          intervention2: {}
        };
        expect(networkMetaAnalysisService.hasInterventionOverlap(interventionOverlapMap)).toBeTruthy();
      });

      it('should return false if there are no interventions in the overlap map', () => {
        var interventionOverlapMap = {
        };
        expect(networkMetaAnalysisService.hasInterventionOverlap(interventionOverlapMap)).toBeFalsy();
      });
    });

    describe('getReferenceArms', () => {
      it('should return the arms which are reference arms of given rows', () => {
        var rows = [{
          trialverseUid: 'refUri',
          referenceArm: 'refUri'
        }, {
          trialverseUid: 'OtherUri',
          referenceArm: 'refUri'
        }];
        var result = networkMetaAnalysisService.getReferenceArms(rows);
        var expectedResult = {
          refUri: {
            trialverseUid: 'refUri',
            referenceArm: 'refUri'
          }
        };
        expect(result).toEqual(expectedResult);
      });
    });

    describe('hasMissingCovariateValues', () => {
      it('should return true if there is a study with at least 2 included arms, and a missing covariate value', function() {
        const tables = {
          absolute:[ {
            numberOfIncludedInterventions:2,
            covariatesColumns: [{
              data: 'NA'
            }]
          }]
        };
        const result = networkMetaAnalysisService.hasMissingCovariateValues(tables);
        expect(result).toBeTruthy();
      });

      it('should return false if there is a study with less then 2 included arms, and a missing covariate value', function() {
        const tables = {
          absolute:[ {
            numberOfIncludedInterventions:1,
            covariatesColumns: [{
              data: 'NA'
            }]
          }]
        };
        const result = networkMetaAnalysisService.hasMissingCovariateValues(tables);
        expect(result).toBeFalsy();
      });

      it('should return false if there is a study with at least 2 included arms, and covariate value', function() {
        const tables = {
          absolute:[ {
            numberOfIncludedInterventions:2,
            covariatesColumns: [{
              data: 12
            }]
          }]
        };
        const result = networkMetaAnalysisService.hasMissingCovariateValues(tables);
        expect(result).toBeFalsy();
      });
    });
  });
});
