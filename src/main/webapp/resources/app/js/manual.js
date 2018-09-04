'use strict';
define([
  'bowser',
  'jquery',
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
  function(bowser, $, renderMathInElement, scrollToTop, tocbot) {
    document.addEventListener('DOMContentLoaded', function() {
      doBowserChecks();
      
      var gemtcShared = require('gemtc-web/app/manual/shared.html');
      increaseHeaderDepth(gemtcShared);
      document.getElementById('gemtc-shared-content').innerHTML = gemtcShared;
      
      var mcdaShared = require('mcda-web/app/manual/shared.html');
      increaseHeaderDepth(mcdaShared);
      document.getElementById('mcda-shared-content').innerHTML = mcdaShared;
      
      initTocbot();
      renderMathInElement(document.body);
      scrollToTop.addBackToTop();

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

      function increaseHeaderDepth(sharedText) {
        $('h4', sharedText).replaceWith(function() {
          return '<h5 id="' + $(this).attr('id') + '">' + $(this).text() +
            '</h5>';
        });
        $('h3', sharedText).replaceWith(function() {
          return '<h4 id="' + $(this).attr('id') + '">' + $(this).text() +
            '</h4>';
        });
        $('h2', sharedText).replaceWith(function() {
          return '<h3 id="' + $(this).attr('id') + '">' + $(this).text() +
            '</h3>';
        });
      }
    });

  });
