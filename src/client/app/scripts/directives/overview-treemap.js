angular.module('cartaoFidelidadeApp')
  .directive('overviewTreemap', ['$window', function ($window) {
    var directiveDefinitionObject = {
        restrict: 'E',
        replace: false,
        scope: {
            grupos: '=chartData'
        },
        link: function (scope, element, attrs) {

          // Browser onresize event
          window.onresize = function() {
            scope.$apply();
          };

          // Watch for resize event
          scope.$watch('grupos', function(oldValue, newValue) {
              scope.render(scope.grupos);
          });

          scope.render = function(data) {
            var margin = {top: 40, right: 10, bottom: 10, left: 10},
                width = 960 - margin.left - margin.right,
                height = 500 - margin.top - margin.bottom;

            var d3 = $window.d3;
            var color = d3.scale.category20c();

            var treemap = d3.layout.treemap()
                .size([width, height])
                .sticky(true)
                .value(function(d) { return d.qtdLicitacoes; });

            var div = d3.select(element[0]).append("div")
                .style("position", "relative")
                .style("width", (width + margin.left + margin.right) + "px")
                .style("height", (height + margin.top + margin.bottom) + "px")
                .style("left", margin.left + "px")
                .style("top", margin.top + "px");

              var node = div.datum(data).selectAll(".node")
                  .data(treemap.nodes)
                .enter().append("div")
                  .attr("class", "node")
                  .call(position)
                  .style("background", function(d) { return color(d.cnpjCpf); })
                  .text(function(d) { return d.children ? null : d.nome; });

              d3.selectAll("input").on("change", function change() {
                var value = this.value === "count"
                    ? function() { return 1; }
                    : function(d) { return d.qtdLicitacoes; };

                node
                    .data(treemap.value(value).nodes)
                  .transition()
                    .duration(1500)
                    .call(position);
              });

            function position() {
              this.style("left", function(d) { return d.x + "px"; })
                  .style("top", function(d) { return d.y + "px"; })
                  .style("width", function(d) { return Math.max(0, d.dx - 1) + "px"; })
                  .style("height", function(d) { return Math.max(0, d.dy - 1) + "px"; });
            }
          }
        }
    }
    return directiveDefinitionObject;
}]);
