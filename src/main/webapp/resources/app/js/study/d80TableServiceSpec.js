'use strict';
define(['angular-mocks', './study'], function(angularMocks) {
  var d80tableservice;
  describe('the d80tableservice', function() {
    var results = [
      [{
        armUri: 'arm1Uri',
        outcomeUri: 'outcome1Uri',
        momentUri: 'primaryMomentUri',
        result_property: 'mean',
        value: 3.6
      }, {
        armUri: 'arm1Uri',
        outcomeUri: 'outcome1Uri',
        momentUri: 'primaryMomentUri',
        result_property: 'sample_size',
        value: 36
      }, {
        armUri: 'arm1Uri',
        outcomeUri: 'outcome1Uri',
        momentUri: 'primaryMomentUri',
        result_property: 'standard_deviation',
        value: 3
      }, {
        armUri: 'arm2Uri',
        outcomeUri: 'outcome1Uri',
        momentUri: 'primaryMomentUri',
        result_property: 'mean',
        value: 13.6
      }, {
        armUri: 'arm2Uri',
        outcomeUri: 'outcome1Uri',
        momentUri: 'primaryMomentUri',
        result_property: 'sample_size',
        value: 136
      }, {
        armUri: 'arm2Uri',
        outcomeUri: 'outcome1Uri',
        momentUri: 'primaryMomentUri',
        result_property: 'standard_deviation',
        value: 13
      }],
      [{
        armUri: 'arm1Uri',
        outcomeUri: 'outcome2Uri',
        momentUri: 'primaryMomentUri',
        result_property: 'count',
        value: 23
      }, {
        armUri: 'arm1Uri',
        outcomeUri: 'outcome2Uri',
        momentUri: 'primaryMomentUri',
        result_property: 'sample_size',
        value: 236
      }, {
        armUri: 'arm2Uri',
        outcomeUri: 'outcome2Uri',
        momentUri: 'primaryMomentUri',
        result_property: 'count',
        value: 33
      }, {
        armUri: 'arm2Uri',
        outcomeUri: 'outcome2Uri',
        momentUri: 'primaryMomentUri',
        result_property: 'sample_size',
        value: 336
      }, {
        armUri: 'arm2Uri',
        outcomeUri: 'outcome2Uri',
        momentUri: 'notPrimaruMomentUri',
        result_property: 'count',
        value: 33
      }, {
        armUri: 'arm2Uri',
        outcomeUri: 'outcome2Uri',
        momentUri: 'notPrimaruMomentUri',
        result_property: 'sample_size',
        value: 336
      }]
    ];
    beforeEach(angular.mock.module('trialverse.study'));
    beforeEach(inject(function(D80TableService) {
      d80tableservice = D80TableService;
    }));

    describe('buildResultsByEndpointAndArm', function() {
      it('should build a map of endpoints to arms to results for the data fo the primary measurement moment', function() {

        var expectedResult = {
          outcome1Uri: {
            arm1Uri: results[0].slice(0, 3),
            arm2Uri: results[0].slice(3)
          },
          outcome2Uri: {
            arm1Uri: results[1].slice(0, 2),
            arm2Uri: results[1].slice(2, 4)
          }
        };

        var result = d80tableservice.buildResultsByEndpointAndArm(results, 'primaryMomentUri');
        expect(result).toEqual(expectedResult);
      });
    });

    describe('buildMeasurements', function() {
      it('should build the measurements from the results primary measurement moment uri and endpoints', function() {
        var endpoints = [{
          uri: 'outcome1Uri',
          measurementType: 'ontology:continuous',
          resultProperties: ['bla#mean', 'bla#sample_size', 'bla#standard_deviation']
        }, {
          uri: 'outcome2Uri',
          measurementType: 'ontology:dichotomous',
          resultProperties: ['bla#count', 'bla#sample_size']
        }];

        var expectedResult = {
          outcome1Uri: {
            arm1Uri: {
              endpointUri: 'outcome1Uri',
              armUri: 'arm1Uri',
              type: 'continuous',
              resultProperties: {
                mean: 3.6,
                sampleSize: 36,
                standardDeviation: 3
              },
              label: '3.60 ± 3.00 (36)'
            },
            arm2Uri: {
              endpointUri: 'outcome1Uri',
              armUri: 'arm2Uri',
              type: 'continuous',
              resultProperties: {
                mean: 13.6,
                sampleSize: 136,
                standardDeviation: 13
              },
              label: '13.60 ± 13.00 (136)'
            }
          },
          outcome2Uri: {
            arm1Uri: {
              endpointUri: 'outcome2Uri',
              armUri: 'arm1Uri',
              type: 'dichotomous',
              resultProperties: {
                count: 23,
                sampleSize: 236
              },
              label: '23/236'
            },
            arm2Uri: {
              endpointUri: 'outcome2Uri',
              armUri: 'arm2Uri',
              type: 'dichotomous',
              resultProperties: {
                count: 33,
                sampleSize: 336
              },
              label: '33/336'
            }
          },
          toBackEndMeasurements: [{
            endpointUri: 'outcome1Uri',
            armUri: 'arm1Uri',
            type: 'continuous',
            resultProperties: {
              mean: 3.6,
              sampleSize: 36,
              standardDeviation: 3
            },
            label: '3.60 ± 3.00 (36)'
          }, {
            endpointUri: 'outcome1Uri',
            armUri: 'arm2Uri',
            type: 'continuous',
            resultProperties: {
              mean: 13.6,
              sampleSize: 136,
              standardDeviation: 13
            },
            label: '13.60 ± 13.00 (136)'
          }, {
            endpointUri: 'outcome2Uri',
            armUri: 'arm1Uri',
            type: 'dichotomous',
            resultProperties: {
              count: 23,
              sampleSize: 236
            },
            label: '23/236'
          }, {
            endpointUri: 'outcome2Uri',
            armUri: 'arm2Uri',
            type: 'dichotomous',
            resultProperties: {
              count: 33,
              sampleSize: 336
            },
            label: '33/336'
          }]
        };

        var result = d80tableservice.buildMeasurements(results, 'primaryMomentUri', endpoints);
        expect(result).toEqual(expectedResult);
      });
    });

    describe('buildEstimateRows', function() {
      it('should build build an array of rows with the label value pairs of the estimates', function() {
        var estimateResults = {
          baselineUri: 'baselineUri',
          estimates: {
            endpoint1Uri: [{
              pointEstimate: 5.1234,
              confidenceIntervalLowerBound: 2.1234,
              confidenceIntervalUpperBound: 5.1234,
              pValue: 0.12346,
              armUri: 'arm1Uri'
            }]
          }
        };
        var endpoints = [{
          measurementType: 'ontology:dichotomous',
          uri: 'endpoint1Uri'
        }];
        var arms = [{
          armURI: 'arm1Uri',
          label: 'arm1Label'
        }, {
          armURI: 'baselineUri'
        }, {
          armURI: 'arm2Uri',
          label: 'arm2Label'
        }];

        var expectedResult = [{
          endpoint: endpoints[0],
          rowLabel: 'Comparison Groups',
          rowValues: [arms[0].label, arms[2].label]
        }, {
          rowLabel: 'Risk ratio',
          rowValues: [estimateResults.estimates.endpoint1Uri[0].pointEstimate.toFixed(2), '<point estimate>']
        }, {
          rowLabel: 'Confidence Interval',
          rowValues: ['(' + estimateResults.estimates.endpoint1Uri[0].confidenceIntervalLowerBound.toFixed(2) +
            ', ' + estimateResults.estimates.endpoint1Uri[0].confidenceIntervalUpperBound.toFixed(2) + ')',
            '<confidence interval>'
          ]
        }, {
          rowLabel: 'P-value',
          rowValues: [estimateResults.estimates.endpoint1Uri[0].pValue.toFixed(2), '<P-value>']
        }];
        var result = d80tableservice.buildEstimateRows(estimateResults, endpoints, arms);
        expect(result).toEqual(expectedResult);
      });
    });

    describe('buildResultLabel', function() {
      it('should return an appropriate label for the given results', function() {
        var resultsObject = {
          type: 'dichotomous',
          resultProperties: {
            count: 5,
            sampleSize: 20
          }
        };
        var expectedLabel = '5/20';
        expect(expectedLabel).toEqual(d80tableservice.buildResultLabel(resultsObject));
        resultsObject = {
          type: 'continuous',
          resultProperties: {
            mean: '55E-1',
            standardDeviation: 0.5,
            sampleSize: 20
          }
        };
        expectedLabel = '5.50 ± 0.50 (20)';
        expect(expectedLabel).toEqual(d80tableservice.buildResultLabel(resultsObject));
        resultsObject = {
          type: 'wrong type',
          resultProperties: {}
        };
        expect(function() {
          d80tableservice.buildResultLabel(resultsObject);
        }).toThrow('unknown measurement type');
      });
      it('should be able to use percentage with event to fill in a missing count', function() {
        var resultsObject = {
          type: 'dichotomous',
          resultProperties: {
            percentage: 9.61,
            sampleSize: 52
          }
        };
        var expectedResult = '5/52';
        var result = d80tableservice.buildResultLabel(resultsObject);
        expect(result).toEqual(expectedResult);
      });
      it('should be able to calculate a missing standard deviation, using the standard error', function() {
        var resultsObject = {
          type: 'continuous',
          resultProperties: {
            mean: '10',
            standardError: 2,
            sampleSize: 100
          }
        };
        var expectedResult = '10.00 ± 20.00 (100)';
        var result = d80tableservice.buildResultLabel(resultsObject);
        expect(result).toEqual(expectedResult);
      });
    });

    describe('buildResultsObject', function() {
      it('should return the appropriate resultsObject', function() {
        var armResults = [{
          result_property: 'count',
          value: 19
        }, {
          result_property: 'sample_size',
          value: 30
        }];
        var endpoint = {
          uri: 'http://endpoint.com/1',
          measurementType: 'ontology:dichotomous',
          resultProperties: ['bla#count', 'bla#sample_size']
        };
        var armUri = 'http://arm.some/1';

        var expectedResult = {
          endpointUri: endpoint.uri,
          armUri: armUri,
          type: 'dichotomous',
          resultProperties: {
            count: 19,
            sampleSize: 30
          }
        };
        expect(d80tableservice.buildResultsObject(armResults, endpoint, armUri)).toEqual(expectedResult);
      });
    });

    describe('buildArmTreatmentsLabel', function() {
      it('should return the appropriate treatment label for the arm', function() {
        var treatments = [{
          treatmentDoseType: 'ontology:FixedDoseDrugTreatment',
          drug: {
            label: 'Floepsetine'
          },
          fixedValue: 10,
          doseUnit: {
            label: 'mg'
          },
          dosingPeriodicity: 'P1D'
        }, {
          treatmentDoseType: 'ontology:TitratedDoseDrugTreatment',
          drug: {
            label: 'Plopsatine'
          },
          minValue: 5,
          maxValue: 10,
          doseUnit: {
            label: 'ml'
          },
          dosingPeriodicity: 'PT12H'
        }];
        var expectedResult = 'Floepsetine 10 mg per 1 day(s) + Plopsatine 5-10 ml per 12 hour(s)';
        expect(d80tableservice.buildArmTreatmentsLabel(treatments)).toEqual(expectedResult);
        expect(function() {
          d80tableservice.buildArmTreatmentsLabel([{
            treatmentDoseType: 'ontology:wrongtype'
          }]);
        }).toThrow('unknown dosage type');
      });
    });
  });
});