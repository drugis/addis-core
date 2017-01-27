'use strict';
define(['angular-mocks'], function() {
  var d80tableservice;
  describe('the d80tableservice', function() {
    beforeEach(module('trialverse.study'));
    beforeEach(inject(function(D80TableService) {
      d80tableservice = D80TableService;
    }));

    describe('buildResultsByEndpointAndArm', function() {
      it('should build a map of endpoints to arms to results for the data fo the primary measurement moment', function() {
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

    });
    describe('buildResultsObject', function() {

    });
    describe('buildArmTreatmentsLabel', function() {
      
    });
  });
});
