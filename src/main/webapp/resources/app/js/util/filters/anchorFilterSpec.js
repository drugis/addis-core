define(['angular', 'angular-mocks'], function () {
  describe('The anchor filter', function () {
    var anchorFilter;

beforeEach(module('trialverse'));
    beforeEach(module('trialverse.util'));
    beforeEach(module('trialverse.measurementMoment'));

    beforeEach(inject(function($filter) {
      anchorFilter = $filter('anchorFilter');
    }));

    it('should pass though undefined measuremoments', function() {
      expect(anchorFilter(undefined)).toEqual(undefined);
    });

    it('should use "at" as preposition when the off set is instantaneous', function() {
      var measurementmoment = {
        // if (!measurementMoment.epoch || !measurementMoment.offset || !measurementMoment.relativeToAnchor) {
        //   return '';
        // }
        // var offsetStr = (measurementMoment.offset === 'PT0S') ? 'At' : $filter('durationFilter')(measurementMoment.offset) + ' from';
        // var anchorStr = measurementMoment.relativeToAnchor === 'http://trials.drugis.org/ontology#anchorEpochStart' ? 'start' : 'end';
        // return offsetStr + ' ' + anchorStr + ' of ' + measurementMoment.epoch.label;
        todo mock measurementmoment
      };

      expect(anchorFilter(measurementmoment)).toEqual(' at epoch start');
      expect(anchorFilter(measurementmoment)).toEqual(' at epoch end');
    }); 

    it('should use "from" as preposition when the off set is instantaneous', function() {
      expect(anchorFilter(measurementmoment)).toEqual(' from epoch start');
      expect(anchorFilter(measurementmoment)).toEqual(' from epoch end');
    }); 

  });
});
