'use strict';
define([
  'lodash',
  'jquery',
  'bowser',
  'katex/dist/contrib/auto-render.min.js',
  'vanilla-back-to-top',
  'tocbot',
  'katex',
  'font-awesome/css/font-awesome.min.css',
  'gemtc-web/app/css/gemtc-drugis.css',
  'mcda-web/app/css/mcda-drugis.css',
  'css/addis-drugis.css',
  'katex/dist/katex.min.css'
],
  function(
    _,
    $,
    bowser,
    renderMathInElement,
    scrollToTop,
    tocbot
  ) {
    document.addEventListener('DOMContentLoaded', function() {
      doBowserChecks();
      loadGemtcManual();
      loadMcdaManual();
      initTocbot();
      renderMathInElement(document.body);
      scrollToTop.addBackToTop();

      function loadGemtcManual() {
        var gemtcShared = require('gemtc-web/app/manual/shared.html');
        var gemtcSharedWithIncreasedHeaderDepth = increaseHeaderDepths(gemtcShared);
        document.getElementById('gemtc-shared-content').innerHTML = gemtcSharedWithIncreasedHeaderDepth;
      }

      function loadMcdaManual() {
        var mcdaShared = require('mcda-web/app/manual/shared.html');
        var mcdaSharedWithIncreasedHeaderDepth = increaseHeaderDepths(mcdaShared);
        document.getElementById('mcda-shared-content').innerHTML = mcdaSharedWithIncreasedHeaderDepth;
      }

      function doBowserChecks() {
        if (bowser.c || (bowser.msie && bowser.version <= 8)) {
          document.getElementById('browserVersion1').innerHTML = bowser.name + ' ' +
            bowser.version;
          document.getElementById('browserCheck').style.display = 'block';
        }
        if (bowser.x) {
          document.getElementById('browserUnknown').style.display = 'block';
        }
      }

      function increaseHeaderDepths(sharedText) {
        var newSharedText = _.cloneDeep(sharedText);
        for (var headerNumber = 4; headerNumber > 2; --headerNumber) {
          increaseHeader(newSharedText, headerNumber);
        }
        return newSharedText;
      }

      function increaseHeader(text, headerNumber) {
        var nextHeader = ++headerNumber;
        $('h' + headerNumber, text).replaceWith(function() {
          return '<h' + nextHeader + ' id="' + $(this).attr('id') + '">' + $(this).text() +
            '</h' + nextHeader + '>';
        });
      }

      function initTocbot() {
        tocbot.init({
          // Where to render the table of contents.
          tocSelector: '#addis-toc',
          // Where to grab the headings to build the table of contents.
          contentSelector: '.js-toc-content',
          // Which headings to grab inside of the contentSelector element.
          headingSelector: 'h2, h3, h4, h5',
          collapseDepth: 5
        });
      }
    });

  });
