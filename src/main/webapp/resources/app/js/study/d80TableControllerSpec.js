'use strict';
define(['angular-mocks'], function () {
  describe('the d80 controller', function () {
    var rootScope;
    var epochServiceMock = jasmine.createSpyObj('EpochService', ['queryItems']);
    var armServiceMock = jasmine.createSpyObj('ArmService', ['queryItems']);
    var activityServiceMock = jasmine.createSpyObj('ActivityService', [
      'queryItems'
    ]);
    var studyDesignServiceMock = jasmine.createSpyObj('StudyDesignService', [
      'queryItems'
    ]);
    var endpointServiceMock = jasmine.createSpyObj('EndpointService', [
      'queryItems'
    ]);
    var measurementMomentServiceMock = jasmine.createSpyObj(
      'MeasurementMomentService',
      ['queryItems']
    );
    var resultsServiceMock = jasmine.createSpyObj('ResultsService', [
      'queryResultsForOutcome'
    ]);
    var estimatesResourceMock = jasmine.createSpyObj('EstimatesResource', [
      'getEstimates'
    ]);
    var d80TableServiceMock = jasmine.createSpyObj('D80TableService', [
      'buildMeasurements',
      'buildEstimateRows',
      'buildArmTreatmentsLabel',
      'getActivityForArm',
      'getInitialMeasurementMoment'
    ]);
    var studyMock;
    var epochs = [
      {
        uri: 'epochUri1'
      },
      {
        uri: 'epochUri2',
        isPrimary: 'truthy'
      }
    ];
    var arms = [
      {
        armURI: 'armUri1'
      },
      {
        armURI: 'armUri2'
      }
    ];
    var activities = [
      {
        activityUri: 'activityUri1',
        treatments: []
      },
      {
        activityUri: 'activityUri2',
        treatments: [
          {
            treatmentDoseType: 'ontology:FixedDoseDrugTreatment',
            doseUnit: {
              label: 'kilo'
            },
            dosingPeriodicity: 'PT2H',
            drug: {
              label: 'poison'
            }
          }
        ]
      }
    ];
    var designCoordinates = [
      {
        armUri: arms[0].armURI,
        epochUri: epochs[1].uri,
        activityUri: activities[1].activityUri
      },
      {
        armUri: arms[1].armURI,
        epochUri: epochs[1].uri,
        activityUri: activities[1].activityUri
      },
      {
        armUri: arms[0].armURI,
        epochUri: epochs[0].uri,
        activityUri: activities[0].activityUri
      },
      {
        armUri: arms[1].armURI,
        epochUri: epochs[0].uri,
        activityUri: activities[0].activityUri
      }
    ];
    var measurementMoments = [
      {
        uri: 'measurementMomentUri1',
        offset: 'PT0S',
        relativeToAnchor: 'ontology:anchorEpochEnd',
        epochUri: epochs[1].uri
      }
    ];
    var endpoints = [
      {
        uri: 'endpointUri1'
      }
    ];
    var results = [
      {
        label: 'foo'
      }
    ];
    var estimates = [];
    var toBackEndMeasurements = {
      id: 'qop'
    };
    var builtMeasurements = {
      toBackEndMeasurements: toBackEndMeasurements
    };
    beforeEach(angular.mock.module('trialverse.study'));
    beforeEach(inject(function ($rootScope, $q, $controller, $filter) {
      function prepareQueryItems(mock, result) {
        var defer = $q.defer();
        mock.queryItems.and.returnValue(defer.promise);
        defer.resolve(result);
      }
      rootScope = $rootScope;
      prepareQueryItems(epochServiceMock, epochs);
      prepareQueryItems(armServiceMock, arms);
      prepareQueryItems(activityServiceMock, activities);
      prepareQueryItems(studyDesignServiceMock, designCoordinates);
      prepareQueryItems(endpointServiceMock, endpoints);
      prepareQueryItems(measurementMomentServiceMock, measurementMoments);

      var resultsDefer = $q.defer();
      resultsServiceMock.queryResultsForOutcome.and.returnValue(
        resultsDefer.promise
      );
      resultsDefer.resolve(results);

      var estimatesDefer = $q.defer();
      estimatesResourceMock.getEstimates.and.returnValue({
        $promise: estimatesDefer.promise
      });
      estimatesDefer.resolve(estimates);

      d80TableServiceMock.getActivityForArm.and.returnValue(activities[1]);
      d80TableServiceMock.buildMeasurements.and.returnValue(builtMeasurements);
      d80TableServiceMock.getInitialMeasurementMoment.and.returnValue(
        measurementMoments[0]
      );

      $controller('D80TableController', {
        $scope: rootScope,
        $filter: $filter,
        $q: $q,
        $modalInstance: {close: function () {}},
        EpochService: epochServiceMock,
        ArmService: armServiceMock,
        ActivityService: activityServiceMock,
        StudyDesignService: studyDesignServiceMock,
        EndpointService: endpointServiceMock,
        MeasurementMomentService: measurementMomentServiceMock,
        ResultsService: resultsServiceMock,
        EstimatesResource: estimatesResourceMock,
        D80TableService: d80TableServiceMock,
        study: studyMock
      });
    }));
    describe('for data with a primary epoch and proper measurement moment', function () {
      describe('on load', function () {
        beforeEach(function () {
          rootScope.$digest();
        });
        it('should query all the things', function () {
          expect(armServiceMock.queryItems).toHaveBeenCalled();
          expect(epochServiceMock.queryItems).toHaveBeenCalled();
          expect(activityServiceMock.queryItems).toHaveBeenCalled();
          expect(studyDesignServiceMock.queryItems).toHaveBeenCalled();
          expect(endpointServiceMock.queryItems).toHaveBeenCalled();
          expect(measurementMomentServiceMock.queryItems).toHaveBeenCalled();
          expect(resultsServiceMock.queryResultsForOutcome).toHaveBeenCalled();

          expect(d80TableServiceMock.buildMeasurements).toHaveBeenCalledWith(
            [results],
            measurementMoments[0].uri,
            endpoints
          );
          expect(rootScope.measurements).toEqual(builtMeasurements);
          expect(estimatesResourceMock.getEstimates).toHaveBeenCalledWith({
            measurements: toBackEndMeasurements,
            baselineUri: 'armUri1'
          });
          expect(d80TableServiceMock.buildEstimateRows).toHaveBeenCalledWith(
            estimates,
            endpoints,
            arms
          );
          expect(rootScope.isMissingPrimary).toBeFalsy();
        });
      });
    });
  });
});
