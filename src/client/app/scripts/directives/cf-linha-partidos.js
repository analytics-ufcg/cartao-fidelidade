(function() {
  'use strict';

  angular.module('cartaoFidelidadeApp')
    .directive('cfLinhaPartidos', cfLinhaPartidos);

    cfLinhaPartidos.$inject = ['$window'];

    /*jshint latedef: nofunc */
    function cfLinhaPartidos($window) {
      return {
        template: '',
        restrict: 'E',
        scope: {
          dados: '='
        },
        link: function(scope, element) {
          var
            d3 = $window.d3;
            // Set the dimensions of the canvas / graph
            var margin = {top: 0, right: 0, bottom: 0, left: 0},
               width = 100 - margin.left - margin.right,
               height = 2 - margin.top - margin.bottom;

            var dataset = [{
                  data: [{
                      month: 'Aug',
                      count: 5
                  }],
                  name: 'Series #1'
              }, {
                  data: [{
                      month: 'Aug',
                      count: 5
                  }],
                  name: 'Series #2'
              }, {
                  data: [{
                      month: 'Aug',
                      count: 5
                  }],
                  name: 'Series #3'
              }

              ],
              series = dataset.map(function (d) {
                  return d.name;
              }),
              dataset = dataset.map(function (d) {
                  return d.data.map(function (o, i) {
                      // Structure it so that your numeric
                      // axis (the stacked amount) is y
                      return {
                          y: o.count,
                          x: o.month
                      };
                  });
              }),
              stack = d3.layout.stack();

          stack(dataset);

          var dataset = dataset.map(function (group) {
              return group.map(function (d) {
                  // Invert the x and y values, and y0 becomes x0
                  return {
                      x: d.y,
                      y: d.x,
                      x0: d.y0
                  };
              });
          });

            var svg = d3.select(element[0])
                .append("svg")
                .attr({
                  'version': '1.1',
                  'viewBox': '0 0 '+(width + margin.left + margin.right)+' '+(height + margin.top + margin.bottom),
                  'width': '100%'})
                .append("g")
                    .attr("transform",
                          "translate(" + margin.left + "," + margin.top + ")");
            var xMax = d3.max(dataset, function (group) {
                  return d3.max(group, function (d) {
                      return d.x + d.x0;
                  });
              }),
              xScale = d3.scale.linear()
                  .domain([0, xMax])
                  .range([0, width]),
              months = dataset[0].map(function (d) {
                  return d.y;
              }),
              yScale = d3.scale.ordinal()
                  .domain(months)
                  .rangeRoundBands([0, height], .1),
              colours = d3.scale.category10(),
              groups = svg.selectAll('g')
                  .data(dataset)
                  .enter()
                  .append('g')
                  .style('fill', function (d, i) {
                  return colours(i);
              }),
              rects = groups.selectAll('rect')
                  .data(function (d) {
                  return d;
              })
                .enter()
                .append('rect')
                .attr('x', function (d) { return xScale(d.x0); })
                .attr('y', 0)
                .attr('height', height)
                .attr('width', function (d) { return xScale(d.x); });

        }
      }
    }
})();
