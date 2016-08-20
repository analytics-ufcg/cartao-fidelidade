(function() {
  'use strict';

  angular.module('cartaoFidelidadeApp')
    .controller('OverviewCtrl', OverviewCtrl);

  OverviewCtrl.$inject = ['Fornecedores'];

  /*jshint latedef: nofunc */
  function OverviewCtrl(Fornecedores) {
    var vm = this;
    vm.overviewData = Fornecedores.query(function(data){
      vm.overviewData = {
        "nome": "todos",
        "children": data
      };
    }, function(err){
      console.log("error")
    });
  }
})();
