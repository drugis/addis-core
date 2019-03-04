'use strict';
define(['angular-mocks'], function(angularMocks) {
  describe('the d80 controller', function() {
    var
      rootScope,
      epochServiceMock = jasmine.createSpyObj('EpochService', ['queryItems']),
      armServiceMock = jasmine.createSpyObj('ArmService', ['queryItems']),
      activityServiceMock = jasmine.createSpyObj('ActivityService', ['queryItems']),
      studyDesignServiceMock = jasmine.createSpyObj('StudyDesignService', ['queryItems']),
      endpointServiceMock = jasmine.createSpyObj('EndpointService', ['queryItems']),
      measurementMomentServiceMock = jasmine.createSpyObj('MeasurementMomentService', ['queryItems']),
      resultsServiceMock = jasmine.createSpyObj('ResultsService', ['queryResultsByOutcome']),
      estimatesResourceMock = jasmine.createSpyObj('EstimatesResource', ['getEstimates']),
      d80TableServiceMock = jasmine.createSpyObj('D80TableService', ['buildMeasurements', 'buildResultLabel', 'buildResultsByEndpointAndArm', 'buildEstimateRows', 'buildArmTreatmentsLabel']),
      studyMock,
      epochs = [{
        uri: 'epochUri1'
      }, {
        uri: 'epochUri2',
        isPrimary: 'truthy'
      }],
      arms = [{
        armURI: 'armUri1'
      }, {
        armURI: 'armUri2'
      }],
      activities = [{
        activityUri: 'activityUri1',
        treatments: []
      }, {
        activityUri: 'activityUri2',
        treatments: [{
          treatmentDoseType: 'ontology:FixedDoseDrugTreatment',
          doseUnit: {
            label: 'kilo'
          },
          dosingPeriodicity: 'PT2H',
          drug: {
            label: 'poison'
          }
        }]
      }],
      designCoordinates = [{
        armUri: arms[0].armURI,
        epochUri: epochs[1].uri,
        activityUri: activities[1].activityUri
      }, {
        armUri: arms[1].armURI,
        epochUri: epochs[1].uri,
        activityUri: activities[1].activityUri
      }, {
        armUri: arms[0].armURI,
        epochUri: epochs[0].uri,
        activityUri: activities[0].activityUri
      }, {
        armUri: arms[1].armURI,
        epochUri: epochs[0].uri,
        activityUri: activities[0].activityUri
      }],
      measurementMoments = [{
        uri: 'measurementMomentUri1',
        offset: 'PT0S',
        relativeToAnchor: 'ontology:anchorEpochEnd',
        epochUri: epochs[1].uri
      }],
      endpoints = [{
        uri: 'endpointUri1'
      }],
      results = [{
        label: 'foo'
      }],
      estimates = [],
      toBackEndMeasurements = {
        id: 'qop'
      },
      builtMeasurements = {
        toBackEndMeasurements: toBackEndMeasurements
      };
    beforeEach(angular.mock.module('trialverse.study'));
    beforeEach(inject(function($rootScope, $q, $controller, $filter) {
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
      resultsServiceMock.queryResultsByOutcome.and.returnValue(resultsDefer.promise);
      resultsDefer.resolve(results);

      var estimatesDefer = $q.defer();
      estimatesResourceMock.getEstimates.and.returnValue({
        $promise: estimatesDefer.promise
      });
      estimatesDefer.resolve(estimates);

      d80TableServiceMock.buildMeasurements.and.returnValue(builtMeasurements);

      $controller('D80TableController', {
        $scope: rootScope,
        $filter: $filter,
        $q: $q,
        $modalInstance: {},
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
    describe('for data with a primary epoch and proper measurement moment', function() {
      describe('on load', function() {
        beforeEach(function() {
          rootScope.$digest();
        });
        it('should query all the things', function() {
          expect(armServiceMock.queryItems).toHaveBeenCalled();
          expect(epochServiceMock.queryItems).toHaveBeenCalled();
          expect(activityServiceMock.queryItems).toHaveBeenCalled();
          expect(studyDesignServiceMock.queryItems).toHaveBeenCalled();
          expect(endpointServiceMock.queryItems).toHaveBeenCalled();
          expect(measurementMomentServiceMock.queryItems).toHaveBeenCalled();
          expect(resultsServiceMock.queryResultsByOutcome).toHaveBeenCalled();

          expect(d80TableServiceMock.buildMeasurements).toHaveBeenCalledWith([results], measurementMoments[0].uri, endpoints);
          expect(rootScope.measurements).toEqual(builtMeasurements);
          expect(estimatesResourceMock.getEstimates).toHaveBeenCalledWith({
            measurements: toBackEndMeasurements,
            baselineUri: 'armUri1'
          });
          expect(d80TableServiceMock.buildEstimateRows).toHaveBeenCalledWith(estimates, endpoints, arms);
          expect(rootScope.isMissingPrimary).toBeFalsy();
        });
      });
    });
    describe('for data without a primary epoch', function() {
      beforeEach(function() {
        delete epochs[1].isPrimary;
        rootScope.$digest();
      });
      it('should set scope.isMissingPrimary to true', function() {
        expect(rootScope.isMissingPrimary).toBeTruthy();
      });
      afterEach(function(){
        epochs[1].isPrimary = true;
      });
    });
    describe('for data with a primary epoch but no measurement moment at the right time', function() {
      beforeEach(function() {
        measurementMoments[0].epochUri = epochs[0].uri;
        rootScope.$digest();
      });
      it('should set scope.isMissingPrimary to true', function() {
        expect(rootScope.isMissingPrimary).toBeTruthy();
      });
      afterEach(function(){
        measurementMoments[0].epochUri = epochs[1].uri;
      });
    });
  });
});