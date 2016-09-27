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
          cpfCnpj: '=',
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

          // scope.$watch(function(scope) { return scope.reservatorioSelecionado }, function(newValue, oldValue) {
          //   var r = newValue.id;
          //   d3.selectAll(".svg-reservatorio").attr("class", "svg-reservatorio");
          //   var point = d3.select("#r"+r).attr("class", "svg-reservatorio svg-reservatorio-highlight");
          //   // console.log(point);
          //   // var x = (800 - point.attr('cx'))* 1.6;
          //   // var y = (400 - point.attr('cy'))* 0.05;
          //   // d3.select("#g-mapa").transition().duration(300).ease("linear").attr("transform", "translate("+x+","+y+")");
          // });

          var mouseOnEvent = function(d) {
            scope.onSelectMunicipio()(d.id);
            scope.$apply();
          };

          function zoomed() {
            features.attr('transform', 'translate(' + d3.event.translate + ')scale(' + d3.event.scale + ')');
          }

          function mapaBrasil(br, fornecedor) {
            var brasil = topojson.feature(br, br.objects.municipios);

            var max = Math.log(d3.max(fornecedor.municipios, function(d) { return d.valorEmpenhos; }));
            var min = Math.log(d3.min(fornecedor.municipios, function(d) { return d.valorEmpenhos; }));

            var quantize = d3.scale.quantize()
              .domain([min, max])
              .range(d3.range(9).map(function(i) { return "category-" + i; }));

            svg.selectAll(".municipio-shape")
              .data(brasil.features)
            .enter().append("path")
              .attr("id", function(d) { return d.id; })
              .attr("class", function(d) {
                return "municipio-"+d.id;
              })
              .attr("d", path)
              .on('click', mouseOnEvent);

            for (var i = 0; i < fornecedor.municipios.length; i++) {
              svg.select(".municipio-"+fornecedor.municipios[i].codMunicipio)
                .classed("category "+quantize(Math.log(fornecedor.municipios[i].valorEmpenhos)), true);
            }
          }

          d3.queue()
            .defer(d3.json, 'scripts/municipios.json')
            .defer(d3.json, RESTAPI.url+'/fornecedores/pb/'+scope.cpfCnpj+'/2008/1')
            .await(desenhaMapa);

          function desenhaMapa(error, br, fornecedor) {
            if (error) { return console.error(error); }
            mapaBrasil(br, fornecedor);
          }
        }
      };
    }
})();
