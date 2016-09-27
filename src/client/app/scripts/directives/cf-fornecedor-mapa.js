(function() {
  'use strict';

  angular.module('cartaoFidelidadeApp')
    .directive('cfFornecedorMapa', cfFornecedorMapa);

    cfFornecedorMapa.$inject = ['$window', 'RESTAPI'];

    /*jshint latedef: nofunc */
    function cfFornecedorMapa($window, RESTAPI) {
      return {
        template: '',
        restrict: 'E',
        scope: {
          fornecedor: '=',
          onSelectMunicipio: '&'
        },
        link: function postLink(scope, element) {
          var
            d3 = $window.d3,
            topojson = $window.topojson,
            width = 800,
            height = 400;
          var projection = d3.geo.mercator()
            .scale(9000)
            .translate([width * 7.7, height * -2.3]);
          var path = d3.geo.path()
            .projection(projection);
          var zoom = d3.behavior.zoom()
              .translate([0, 0])
              .scale(1)
              .scaleExtent([1, 8])
              .on('zoom', zoomed);
          var svg = d3.select(element[0])
            .append('svg')
            .attr({
              'version': '1.1',
              'viewBox': '0 0 '+width+' '+height,
              'width': '100%',
              'class': 'svg-map'})
            .call(zoom);

          var features = svg.append('g').attr('id', 'g-map');

          scope.$watch(function(scope) { return scope.fornecedor }, function(newValue, oldValue) {
            if (!newValue.municipios) { return; }
            var max = Math.log(d3.max(newValue.municipios, function(d) { return d.valorEmpenhos; }));
            var min = Math.log(d3.min(newValue.municipios, function(d) { return d.valorEmpenhos; }));
            var quantize = d3.scale.quantize()
              .domain([min, max])
              .range(d3.range(9).map(function(i) { return "category-" + i; }));
            for (var i = 0; i < 9; i++) {
              d3.selectAll(".municipio-shape").classed("category-"+i, false);
            }
            d3.selectAll(".municipio-shape").classed("category", false);
            for (var i = 0; i < newValue.municipios.length; i++) {
              svg.select(".municipio-"+newValue.municipios[i].codMunicipio)
                .classed("category "+quantize(Math.log(newValue.municipios[i].valorEmpenhos)), true);
            }
          });

          var mouseOnEvent = function(d) {
            scope.onSelectMunicipio()(d.id);
            scope.$apply();
          };

          function zoomed() {
            features.attr('transform', 'translate(' + d3.event.translate + ')scale(' + d3.event.scale + ')');
          }

          function mapaBrasil(br) {
            var brasil = topojson.feature(br, br.objects.municipios);

            svg.selectAll(".municipio-shape")
              .data(brasil.features)
            .enter().append("path")
              .attr("id", function(d) { return d.id; })
              .attr("class", function(d) {
                return "municipio-shape municipio-"+d.id;
              })
              .attr("d", path)
              .on('click', mouseOnEvent);
          }

          d3.queue()
            .defer(d3.json, 'scripts/municipios.json')
            .await(desenhaMapa);

          function desenhaMapa(error, br) {
            if (error) { return console.error(error); }
            mapaBrasil(br);
          }
        }
      };
    }
})();
